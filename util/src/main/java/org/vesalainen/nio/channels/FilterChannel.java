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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.lang.Casts;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.function.IOFunction;

/**
 * SeekableFilterChannel provides channel interface for stream filtering.
 * <p>
 * Example:
 * <code>
   try (SeekableFilterChannel xzChannel = new SeekableFilterChannel(channel, 4096, 512, XZInputStream::new, null))
   {
       load(xzChannel, root);
   }
 </code>
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterChannel<T extends ByteChannel> implements ByteChannel, ScatteringSupport, GatheringSupport
{
    protected T channel;
    protected InputStream in;
    protected byte[] buf;
    protected int offset;
    protected int length;
    protected OutputStream out;
    protected long position;
    protected int bufSize = 4096;
    protected Lock readLock = new ReentrantLock();
    protected Lock writeLock = new ReentrantLock();
    protected ByteBuffer outBuf;
    protected ByteBuffer inBuf;
    /**
     * Creates FilterSeekableByteChannel. Only one of in/out functions is allowed.
     * @param channel
     * @param bufSize Buffer size
     * @param fin
     * @param fout
     * @throws IOException 
     */
    public FilterChannel(
            T channel, 
            int bufSize, 
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
        if (fin != null)
        {
            this.in = fin.apply(new Input());
        }
        if (fout != null)
        {
            this.out = fout.apply(new Output());
        }
        this.buf = new byte[bufSize];
    }
    @Override
    public boolean isOpen()
    {
        return in != null || out != null;
    }
    @Override
    public void close() throws IOException
    {
        if (in != null)
        {
            in.close();
            in = null;
        }
        if (out != null)
        {
            out.close();
            out = null;
        }
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        ensureReading();
        readLock();
        try
        {
            int res = 0;
            while (dst.hasRemaining())
            {
                if (length == 0)
                {
                    length = in.read(buf);
                    if (length == -1)
                    {
                        length = 0;
                        if (res > 0)
                        {
                            return res;
                        }
                        return -1;
                    }
                    offset = 0;
                }
                int count = ByteBuffers.move(buf, offset, length, dst);
                offset += count;
                length -= count;
                position += count;
                res += count;
            }
            return res;
        }
        finally
        {
            readUnlock();
        }
    }
    @Override
    public int write(ByteBuffer src) throws IOException
    {
        ensureWriting();
        writeLock();
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
            writeUnlock();
        }
    }
    private void ensureReading() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (in == null)
        {
            throw new NonReadableChannelException();
        }
    }

    private void ensureWriting() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (out == null)
        {
            throw new NonWritableChannelException();
        }
    }


    private class Output extends OutputStream
    {
        private byte[] buf = new byte[1];

        public Output()
        {
            outBuf = ByteBuffer.allocateDirect(bufSize);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException
        {
            while (len > 0)
            {
                outBuf.clear();
                int count = ByteBuffers.move(buf, off, len, outBuf);
                outBuf.flip();
                ChannelHelper.writeAll(channel, outBuf);
                off += count;
                len -= count;
            }
        }

        @Override
        public void write(int b) throws IOException
        {
            buf[0] = (byte) b;
            write(buf);
        }
        
    }

    @Override
    public void readLock()
    {
        readLock.lock();
    }

    @Override
    public void readUnlock()
    {
        readLock.lock();
    }

    @Override
    public void writeLock()
    {
        writeLock.lock();
    }

    @Override
    public void writeUnlock()
    {
        writeLock.unlock();
    }

    private class Input extends InputStream
    {

        public Input()
        {
            inBuf = ByteBuffer.allocateDirect(bufSize);
            inBuf.compact();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (fill())
            {
                return ByteBuffers.move(inBuf, b, off, len);
            }
            else
            {
                return -1;
            }
        }
        
        private boolean fill() throws IOException
        {
            if (!inBuf.hasRemaining())
            {
                inBuf.clear();
                int rc = channel.read(inBuf);
                if (rc == -1)
                {
                    inBuf.compact();
                    return false;
                }
                inBuf.flip();
            }
            return true;
        }
        @Override
        public int read() throws IOException
        {
            if (fill())
            {
                return Casts.castUnsignedInt(inBuf.get());
            }
            else
            {
                return -1;
            }
        }

    }
}
