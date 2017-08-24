/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.vfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.nio.channels.ChannelHelper;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileChannel extends FileChannel
{
    private Path path;
    private VirtualFile file;
    private ByteBuffer bb;
    private Set<? extends OpenOption> options;
    private Lock readLock = new ReentrantLock();
    private Lock writeLock = new ReentrantLock();

    public VirtualFileChannel(Path path, VirtualFile file, Set<? extends OpenOption> options)
    {
        this.path = path;
        this.file = file;
        this.options = options;
        this.bb = file.writeView();
        if (options.contains(TRUNCATE_EXISTING) && options.contains(WRITE))
        {
            file.truncate(0);
        }
        if (options.contains(APPEND))
        {
            bb.position(file.getSize());
        }
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        readLock.lock();
        try
        {
            if (!options.contains(READ))
            {
                throw new NonReadableChannelException();
            }
            bb.limit(file.getSize());
            if (!bb.hasRemaining())
            {
                return -1;
            }
            return (int) ByteBuffers.move(bb, dst);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        writeLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (!options.contains(WRITE))
            {
                throw new NonWritableChannelException();
            }
            bb.limit(bb.capacity());
            int rc = (int) ByteBuffers.move(src, bb);
            file.append(bb.position());
            return rc;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public long position() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        return bb.position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException
    {
        writeLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (newPosition < 0)
            {
                throw new IllegalArgumentException(newPosition+" < 0");
            }
            bb.position((int) newPosition);
            return this;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public long size() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        return file.getSize();
    }

    @Override
    public FileChannel truncate(long size) throws IOException
    {
        writeLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (!options.contains(WRITE))
            {
                throw new NonWritableChannelException();
            }
            if (size < 0)
            {
                throw new IllegalArgumentException(size+" < 0");
            }
            if (size <= file.getSize())
            {
                file.truncate((int) size);
            }
            return this;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void force(boolean metaData) throws IOException
    {
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException
    {
        readLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (!options.contains(READ))
            {
                throw new NonWritableChannelException();
            }
            int avail = (int) (file.getSize()-position);
            if (avail > 0)
            {
                ByteBuffer view = bb.duplicate();
                view.position((int) position);
                if (avail > count)
                {
                    view.limit(file.getSize());
                }
                else
                {
                    view.limit((int) (position+avail));
                }
                return target.write(view);
            }
            return 0;
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException
    {
        writeLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (!options.contains(WRITE))
            {
                throw new NonWritableChannelException();
            }
            if (position <= file.getSize())
            {
                ByteBuffer view = bb.duplicate();
                view.position((int) position);
                view.limit((int) (position+count));
                return src.read(view);
            }
            return 0;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException
    {
        readLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (position < 0)
            {
                throw new IllegalArgumentException(position+" < 0");
            }
            if (!options.contains(READ))
            {
                throw new NonReadableChannelException();
            }
            if (position >= file.getSize())
            {
                return -1;
            }
            if (position <= file.getSize())
            {
                ByteBuffer view = bb.duplicate();
                view.position((int) position);
                view.limit(file.getSize());
                return (int) ByteBuffers.move(view, dst);
            }
            return 0;
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException
    {
        writeLock.lock();
        try
        {
            if (!isOpen())
            {
                throw new ClosedChannelException();
            }
            if (position < 0)
            {
                throw new IllegalArgumentException(position+" < 0");
            }
            if (!options.contains(WRITE))
            {
                throw new NonWritableChannelException();
            }
            ByteBuffer view = bb.duplicate();
            view.position((int) position);
            view.limit(bb.capacity());
            return (int) ByteBuffers.move(src, view);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void implCloseChannel() throws IOException
    {
        if (options.contains(DELETE_ON_CLOSE))
        {
            Files.deleteIfExists(path);
        }
    }
    
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return ChannelHelper.read(this, dsts, offset, length);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        return ChannelHelper.write(this, srcs, offset, length);
    }

}
