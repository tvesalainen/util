/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOption;
import static java.net.StandardSocketOptions.*;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChannelHelper
{
    public static void writeAll(GatheringByteChannel ch, ByteBuffer... bbs) throws IOException
    {
        writeAll(ch, bbs, 0, bbs.length);
    }
    /**
     * Attempts to write all remaining data in bbs.
     * @param ch Target
     * @param bbs 
     * @param offset
     * @param length
     * @throws IOException If couldn't write all.
     */
    public static void writeAll(GatheringByteChannel ch, ByteBuffer[] bbs, int offset, int length) throws IOException
    {
        long all = 0;
        for (int ii=0;ii<length;ii++)
        {
            all += bbs[offset+ii].remaining();
        }
        int count = 0;
        while (all > 0)
        {
            long rc = ch.write(bbs, offset, length);
            if (rc == 0)
            {
                count++;
            }
            else
            {
                all -= rc;
            }
            if (count > 100)
            {
                throw new IOException("Couldn't write all.");
            }
        }
    }
    public static void writeAll(WritableByteChannel ch, ByteBuffer bb) throws IOException
    {
        int count = 0;
        while (bb.hasRemaining())
        {
            int rc = ch.write(bb);
            if (rc == 0)
            {
                count++;
            }
            if (count > 100)
            {
                throw new IOException("Couldn't write all.");
            }
        }
    }
    public static OutputStream newGatheringOutputStream(ByteBuffer[] srcs, int offset, int length)
    {
        return new ByteBufferOutputStream(srcs, offset, length);
    }
    public static GatheringByteChannel newGatheringByteChannel(WritableByteChannel channel)
    {
        return new GatheringByteChannelImpl(channel);
    }
    public static ScatteringByteChannel newScatteringByteChannel(ReadableByteChannel channel)
    {
        return new ScatteringByteChannelImpl(channel);
    }
    public static SocketByteChannel newSocketByteChannel(Socket socket) throws IOException
    {
        return new SocketByteChannel(socket);
    }
    public static ReadableByteChannel newReadableByteChannel(InputStream in)
    {
        return new ReadableByteChannelImpl(in);
    }
    public static WritableByteChannel newWritableByteChannel(OutputStream out)
    {
        return new WritableByteChannelImpl(out);
    }
    public static class WritableByteChannelImpl implements WritableByteChannel
    {
        private static final int BufferSize = 4096;
        private OutputStream out;
        private boolean closed;
        private byte[] buf = new byte[BufferSize];

        public WritableByteChannelImpl(OutputStream out)
        {
            this.out = out;
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            if (src.hasArray())
            {
                byte[] array = src.array();
                int position = src.position();
                int remaining = src.remaining();
                out.write(array, position, remaining);
                src.position(position + remaining);
                return remaining;
            }
            else
            {
                int remaining = src.remaining();
                int length = Math.min(remaining, buf.length);
                src.get(buf, 0, length);
                out.write(buf, 0, length);
                return length;
            }
        }

        @Override
        public boolean isOpen()
        {
            return !closed;
        }

        @Override
        public void close() throws IOException
        {
            out.close();
            closed = true;
        }
        
    }
    public static class ReadableByteChannelImpl implements ReadableByteChannel
    {
        private static final int BufferSize = 4096;
        private InputStream in;
        private boolean closed;
        private byte[] buf = new byte[BufferSize];

        public ReadableByteChannelImpl(InputStream in)
        {
            this.in = in;
        }
        
        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            if (dst.hasArray())
            {
                byte[] array = dst.array();
                int position = dst.position();
                int remaining = dst.remaining();
                int rc = in.read(array, position, remaining);
                if (rc > 0)
                {
                    dst.position(position + rc);
                }
                return rc;
            }
            else
            {
                int remaining = dst.remaining();
                int rc = in.read(buf, 0, Math.min(remaining, buf.length));
                if (rc > 0)
                {
                    dst.put(buf, 0, rc);
                }
                return rc;
            }
        }

        @Override
        public boolean isOpen()
        {
            return !closed;
        }

        @Override
        public void close() throws IOException
        {
            in.close();
            closed = true;
        }
        
    }
    public static class SocketByteChannel implements ByteChannel
    {
        private Socket socket;
        private final ReadableByteChannel in;
        private final WritableByteChannel out;

        public SocketByteChannel(Socket socket) throws IOException
        {
            this.socket = socket;
            in = newReadableByteChannel(socket.getInputStream());
            out = newWritableByteChannel(socket.getOutputStream());
        }
        public <T> void setOption(SocketOption<T> name, T value) throws SocketException
        {
            if (SO_KEEPALIVE.equals(name))
            {
                socket.setKeepAlive((Boolean)value);
            }
            else
            {
                if (SO_LINGER.equals(name))
                {
                    Integer v = (Integer) value;
                    socket.setSoLinger(v >= 0, v);
                }
                else
                {
                    if (SO_REUSEADDR.equals(name))
                    {
                        socket.setReuseAddress((Boolean)value);
                    }
                    else
                    {
                        if (TCP_NODELAY.equals(name))
                        {
                            socket.setReuseAddress((Boolean)value);
                        }
                        else
                        {
                            throw new UnsupportedOperationException(name+" not supported");
                        }
                    }
                }
            }
        }
        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            return in.read(dst);
        }

        @Override
        public boolean isOpen()
        {
            return !socket.isClosed();
        }

        @Override
        public void close() throws IOException
        {
            socket.close();
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            return out.write(src);
        }

        @Override
        public String toString()
        {
            return "SocketByteChannel{" + socket + '}';
        }
        
    }
    public static class ScatteringByteChannelImpl implements ScatteringByteChannel
    {
        private ReadableByteChannel channel;

        public ScatteringByteChannelImpl(ReadableByteChannel channel)
        {
            this.channel = channel;
        }
        
        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
        {
            long res = 0;
            for  (int ii=0;ii<length;ii++)
            {
                ByteBuffer bb = dsts[ii+offset];
                if (bb.hasRemaining())
                {
                    int rc = channel.read(bb);
                    if (rc == -1)
                    {
                        if (res == 0)
                        {
                            return -1;
                        }
                        else
                        {
                            return res;
                        }
                    }
                    res += rc;
                    if (bb.hasRemaining())
                    {
                        break;
                    }
                }
            }
            return res;
        }

        @Override
        public long read(ByteBuffer[] dsts) throws IOException
        {
            return read(dsts, 0, dsts.length);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            return channel.read(dst);
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

    }
    public static class GatheringByteChannelImpl implements GatheringByteChannel
    {
        private WritableByteChannel channel;

        public GatheringByteChannelImpl(WritableByteChannel channel)
        {
            this.channel = channel;
        }
        
        @Override
        public synchronized long write(ByteBuffer[] srcs, int offset, int length) throws IOException
        {
            long res = 0;
            for  (int ii=0;ii<length;ii++)
            {
                ByteBuffer bb = srcs[ii+offset];
                if (bb.hasRemaining())
                {
                    res += channel.write(bb);
                    if (bb.hasRemaining())
                    {
                        break;
                    }
                }
            }
            return res;
        }

        @Override
        public long write(ByteBuffer[] srcs) throws IOException
        {
            return write(srcs, 0, srcs.length);
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            return channel.write(src);
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

    }

}
