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
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.function.IOFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class FilterSeekableByteChannel implements SeekableByteChannel
{
    protected static final int BUF_SIZE = 4096;
    protected ByteChannel channel;
    private InputStream in;
    private byte[] buf;
    private int offset;
    private int length;
    private OutputStream out;
    private long position;

    private FilterSeekableByteChannel(
            ByteChannel channel, 
            IOFunction<? super InputStream,? extends InputStream> fin,
            IOFunction<? super OutputStream,? extends OutputStream> fout
    ) throws IOException
    {
        this.channel = channel;
        if (fin != null)
        {
            this.in = fin.apply(new Input());
        }
        if (fout != null)
        {
            this.out = fout.apply(new Output());
        }
        this.buf = new byte[BUF_SIZE];
    }

    public static FilterSeekableByteChannel openInput(ByteChannel channel, IOFunction<? super InputStream,? extends InputStream> fin) throws IOException
    {
        return new FilterSeekableByteChannel(channel, fin, null);
    }
    public static FilterSeekableByteChannel openOutput(ByteChannel channel, IOFunction<? super OutputStream,? extends OutputStream> fout) throws IOException
    {
        return new FilterSeekableByteChannel(channel, null, fout);
    }
    @Override
    public long position() throws IOException
    {
        return position;
    }
    /**
     * Throws UnsupportedOperationException
     * @param newPosition
     * @return
     * @throws IOException 
     */
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * Throws UnsupportedOperationException
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
    @Override
    public int write(ByteBuffer src) throws IOException
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

    private class Output extends OutputStream
    {
        private ByteBuffer bb = ByteBuffer.allocateDirect(BUF_SIZE);

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
        private ByteBuffer bb = ByteBuffer.allocateDirect(BUF_SIZE);
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (!bb.hasRemaining())
            {
                bb.clear();
                int rc = channel.read(bb);
                if (rc == -1)
                {
                    return -1;
                }
            }
            return ByteBuffers.move(bb, b, off, len);
        }
        
        @Override
        public int read() throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
