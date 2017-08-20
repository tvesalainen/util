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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import static java.nio.file.StandardOpenOption.*;
import java.util.Set;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.nio.channels.ChannelHelper;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VFileChannel extends FileChannel
{
    private VFile file;
    private ByteBuffer bb;
    private Set<? extends OpenOption> options;

    public VFileChannel(VFile file, Set<? extends OpenOption> options)
    {
        this.file = file;
        this.options = options;
        this.bb = file.duplicate();
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
        if (!options.contains(READ))
        {
            throw new IOException("file is not open for reading");
        }
        bb.limit(file.getSize());
        return (int) ByteBuffers.move(bb, dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        if (!options.contains(WRITE))
        {
            throw new IOException("file is not open for writing");
        }
        bb.limit(bb.capacity());
        int rc = (int) ByteBuffers.move(src, bb);
        file.append(bb.position());
        return rc;
    }

    @Override
    public long position() throws IOException
    {
        return bb.position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException
    {
        bb.position((int) newPosition);
        return this;
    }

    @Override
    public long size() throws IOException
    {
        return file.getSize();
    }

    @Override
    public FileChannel truncate(long size) throws IOException
    {
        file.truncate((int) size);
        return this;
    }

    @Override
    public void force(boolean metaData) throws IOException
    {
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException
    {
        if (!options.contains(READ))
        {
            throw new IOException("file is not open for reading");
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

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException
    {
        if (!options.contains(WRITE))
        {
            throw new IOException("file is not open for writing");
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

    @Override
    public int read(ByteBuffer dst, long position) throws IOException
    {
        if (!options.contains(READ))
        {
            throw new IOException("file is not open for reading");
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

    @Override
    public int write(ByteBuffer src, long position) throws IOException
    {
        if (!options.contains(WRITE))
        {
            throw new IOException("file is not open for writing");
        }
        ByteBuffer view = bb.duplicate();
        view.position((int) position);
        view.limit(bb.capacity());
        return (int) ByteBuffers.move(src, view);
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void implCloseChannel() throws IOException
    {
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
