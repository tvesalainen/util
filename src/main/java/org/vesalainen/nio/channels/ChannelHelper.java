/*
 * Copyright (C) 2015 tkv
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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import org.vesalainen.util.ThreadSafeTemporary;

/**
 *
 * @author tkv
 */
public class ChannelHelper
{
    public static void writeAll(GatheringByteChannel ch, ByteBuffer... bbs) throws IOException
    {
        writeAll(ch, bbs, 0, bbs.length);
    }
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
    public static ByteChannel newByteChannel(Socket socket) throws IOException
    {
        return new ByteChannelImpl(socket);
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
        private ThreadSafeTemporary<byte[]> bufferStore = new ThreadSafeTemporary<>(()->{return new byte[BufferSize];});

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
                byte[] array = bufferStore.get();
                int remaining = src.remaining();
                int length = Math.min(remaining, array.length);
                src.get(array, 0, length);
                out.write(array, 0, length);
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
        private ThreadSafeTemporary<byte[]> bufferStore = new ThreadSafeTemporary<>(()->{return new byte[BufferSize];});

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
                byte[] array = bufferStore.get();
                int remaining = dst.remaining();
                int rc = in.read(array, 0, Math.min(remaining, array.length));
                if (rc > 0)
                {
                    dst.put(array, 0, rc);
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
    public static class ByteChannelImpl implements ByteChannel
    {
        private Socket socket;
        private final ReadableByteChannel in;
        private final WritableByteChannel out;

        public ByteChannelImpl(Socket socket) throws IOException
        {
            this.socket = socket;
            in = newReadableByteChannel(socket.getInputStream());
            out = newWritableByteChannel(socket.getOutputStream());
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
