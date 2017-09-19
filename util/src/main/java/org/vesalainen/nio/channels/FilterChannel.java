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

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
 * FilterChannel provides channel interface for stream filtering.
 * <p>
 * Example:
 * <code>
   try (FilterChannel xzChannel = new FilterChannel(channel, 4096, 512, XZInputStream::new, null))
   {
       load(xzChannel, root);
   }
* </code>
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterChannel implements SeekableByteChannel, Flushable
{
    protected SeekableByteChannel channel;
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
    private boolean isClosed;
    private ByteBuffer outBuf;
    private ByteBuffer inBuf;
    /**
     * Creates FilterSeekableByteChannel. Only one of in/out functions is allowed.
     * @param channel
     * @param bufSize Buffer size
     * @param maxSkipSize Maximum forward position size
     * @param fin
     * @param fout
     * @throws IOException 
     */
    public FilterChannel(
            SeekableByteChannel channel, 
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
    /**
     * Returns unfiltered position.
     * @return
     * @throws IOException 
     */
    @Override
    public long position() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        return position;
    }
    /**
     * Changes unfiltered position. Only forward direction is allowed with
     * small skips. This method is for alignment purposes mostly.
     * @param newPosition
     * @return
     * @throws IOException 
     */
    @Override
    public FilterChannel position(long newPosition) throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
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
        throw new UnsupportedOperationException("Not supported.");
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
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public boolean isOpen()
    {
        return !isClosed;
    }
    /**
     * Calls flush() and sets closed. Doesn't close underlying channel!
     * @throws IOException 
     */
    @Override
    public void close() throws IOException
    {
        flush();
        isClosed = true;
    }
    /**
     * For output calls flush() in underlying OutputStream.
     * For input reads underlying InputStream until -1. Underlying channel is
     * positioned to the next byte after.
     * @throws IOException 
     */
    @Override
    public void flush() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (in != null)
        {
            ByteBuffer bb = ByteBuffer.allocate(bufSize);
            int rc = read(bb);
            while (rc != -1)
            {
                bb.clear();
                rc = read(bb);
            }
            channel.position(channel.position()-inBuf.remaining());
            inBuf.clear();
            inBuf.compact();
        }
        else
        {
            out.flush();
        }
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        ensureReading();
        readLock.lock();
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
                        if (res > 0)
                        {
                            length = 0;
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
            readLock.unlock();
        }
    }
    @Override
    public int write(ByteBuffer src) throws IOException
    {
        ensureWriting();
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
            buf[0] = Casts.castUnsignedByte(b);
            write(buf);
        }
        
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
