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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import static java.nio.file.StandardOpenOption.READ;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.lang.Casts;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.function.IOFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterSeekableByteChannel implements SeekableByteChannel
{
    protected ByteChannel channel;
    private InputStream in;
    private byte[] buf;
    private int offset;
    private int length;
    private OutputStream out;
    private long position;
    private int bufSize = 4096;
    private int maxSkipSize;
    private ByteBuffer skipBuffer;
    private Lock readLock = new ReentrantLock();
    private Lock writeLock = new ReentrantLock();
    private boolean closeChannel;
    private boolean isClosed;
    /**
     * Creates FilterSeekableByteChannel. Only one of in/out functions is allowed.
     * @param channel
     * @param bufSize Buffer size
     * @param maxSkipSize Maximum forward position size
     * @param fin
     * @param fout
     * @throws IOException 
     */
    public FilterSeekableByteChannel(
            ByteChannel channel, 
            int bufSize, 
            int maxSkipSize, 
            IOFunction<? super InputStream,? extends InputStream> fin,
            IOFunction<? super OutputStream,? extends OutputStream> fout
    ) throws IOException
    {
        if ((fin != null) && (fout != null))
        {
            throw new IllegalArgumentException("Only one of in/out functions is allowed.");
        }
        this.channel = channel;
        this.bufSize = bufSize;
        this.maxSkipSize = maxSkipSize;
        if (fin != null)
        {
            this.in = fin.apply(new Input());
        }
        if (fout != null)
        {
            this.out = fout.apply(new Output());
        }
        this.buf = new byte[bufSize];
        if (maxSkipSize > 0)
        {
            skipBuffer = ByteBuffer.allocate(maxSkipSize);
        }
    }

    @Override
    public long position() throws IOException
    {
        return position;
    }
    /**
     * Changes un filtered position. Only forward direction is allowed with
     * small skips. This method is for alignment purposes mostly.
     * @param newPosition
     * @return
     * @throws IOException 
     */
    @Override
    public FilterSeekableByteChannel position(long newPosition) throws IOException
    {
        int skip = (int) (newPosition - position());
        if (skip < 0)
        {
            throw new UnsupportedOperationException("backwards position not supported");
        }
        if (skip > skipBuffer.capacity())
        {
            throw new UnsupportedOperationException(skip+" skip not supported maxSkipSize="+maxSkipSize);
        }
        if (skip > 0)
        {
            if (skipBuffer == null)
            {
                throw new UnsupportedOperationException("skip not supported maxSkipSize="+maxSkipSize);
            }
            skipBuffer.clear();
            skipBuffer.limit(skip);
            if (in != null)
            {
                read(skipBuffer);
            }
            else
            {
                write(skipBuffer);
            }
        }
        return this;
    }
    /**
     * Throws UnsupportedOperationException
     * @return
     * @throws IOException 
     */
    @Override
    public long size() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * 
     * @param size
     * @return
     * @throws IOException 
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        readLock.lock();
        try
        {
            if (length == 0)
            {
                length = in.read(buf);
                if (length == -1)
                {
                    return -1;
                }
                offset = 0;
            }
            int count = ByteBuffers.move(buf, offset, length, dst);
            offset += count;
            length -= count;
            position += count;
            return count;
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
            int res = src.remaining();
            while (src.hasRemaining())
            {
                int count = ByteBuffers.move(src, buf, 0, buf.length);
                out.write(buf, 0, count);
            }
            position += res;
            return res;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private class Output extends OutputStream
    {
        private ByteBuffer bb = ByteBuffer.allocateDirect(bufSize);

        @Override
        public void write(byte[] buf, int off, int len) throws IOException
        {
            while (len > 0)
            {
                bb.clear();
                int count = ByteBuffers.move(buf, off, len, bb);
                bb.flip();
                ChannelHelper.writeAll(channel, bb);
                off += count;
                len -= count;
            }
        }

        @Override
        public void write(int b) throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private class Input extends InputStream
    {
        private ByteBuffer bb;

        public Input()
        {
            bb = ByteBuffer.allocateDirect(bufSize);
            bb.compact();
        }
        
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (fill())
            {
                return ByteBuffers.move(bb, b, off, len);
            }
            else
            {
                return -1;
            }
        }
        
        private boolean fill() throws IOException
        {
            if (!bb.hasRemaining())
            {
                bb.clear();
                int rc = channel.read(bb);
                if (rc == -1)
                {
                    return false;
                }
                bb.flip();
            }
            return true;
        }
        @Override
        public int read() throws IOException
        {
            if (fill())
            {
                return Casts.castUnsignedInt(bb.get());
            }
            else
            {
                return -1;
            }
        }

    }
}
