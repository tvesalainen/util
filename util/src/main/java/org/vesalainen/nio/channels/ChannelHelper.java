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
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ChannelHelper
{
    /**
     * Increments position so that position mod align == 0
     * @param ch
     * @param align
     * @throws IOException 
     */
    public static final void align(SeekableByteChannel ch, long align) throws IOException
    {
        ch.position(alignedPosition(ch, align));
    }
    /**
     * Returns Incremented position so that position mod align == 0, but doesn't
     * change channels position.
     * @param ch
     * @param align
     * @return
     * @throws IOException 
     */
    public static final long alignedPosition(SeekableByteChannel ch, long align) throws IOException
    {
        long position = ch.position();
        long mod = position % align;
        if (mod > 0)
        {
            return position + align - mod;
        }
        else
        {
            return position;
        }
    }
    /**
     * Adds skip to position.
     * @param ch
     * @param skip
     * @throws IOException 
     */
    protected static final void skip(SeekableByteChannel ch, long skip) throws IOException
    {
        ch.position(ch.position() + skip);
    }

    /**
     * ScatteringChannel support
     * @param channel
     * @param dsts
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     */
    public static final long read(ReadableByteChannel channel, ByteBuffer[] dsts, int offset, int length) throws IOException
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
    /**
     * ScatteringChannel support
     * @param channel
     * @param dsts
     * @return
     * @throws IOException 
     */
    public static final long read(ReadableByteChannel channel, ByteBuffer[] dsts) throws IOException
    {
        return read(channel, dsts, 0, dsts.length);
    }
    /**
     * GatheringChannel support.
     * @param channel
     * @param srcs
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     */
    public static final long write(WritableByteChannel channel, ByteBuffer[] srcs, int offset, int length) throws IOException
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
    /**
     * GatheringChannel support.
     * @param channel
     * @param srcs
     * @return
     * @throws IOException 
     */
    public static final long write(WritableByteChannel channel, ByteBuffer[] srcs) throws IOException
    {
        return write(channel, srcs, 0, srcs.length);
    }

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
    /**
     * Writes to ch until no remaining left or throws IOException
     * @param ch
     * @param bb
     * @throws IOException 
     */
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
            return ChannelHelper.read(channel, dsts, offset, length);
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
            return ChannelHelper.write(channel, srcs, offset, length);
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
    /**
     * Returns a ByteChannel having feature that for every read/write method the
     * tracer function is called with read/write data between position and limit.
     * <p>
     * This is planned to support calculating digestives.
     * @param channel
     * @param tracer
     * @return 
     */
    public static final ByteChannel traceableChannel(ByteChannel channel, Consumer<ByteBuffer> tracer)
    {
        return new TraceableByteChannel(channel, tracer);
    }
    /**
     * Returns a SeeekableByteChannel having feature that for every read/write method the
     * tracer function is called with read/write data between position and limit.
     * <p>
     * This is planned to support calculating digestives.
     * @param channel
     * @param tracer
     * @return 
     */
    public static final SeekableByteChannel traceableChannel(SeekableByteChannel channel, Consumer<ByteBuffer> tracer)
    {
        return new TraceableSeekableByteChannel(channel, tracer);
    }
    public static class TraceableSeekableByteChannel<T extends SeekableByteChannel> extends TraceableByteChannel<SeekableByteChannel> implements SeekableByteChannel
    {

        public TraceableSeekableByteChannel(SeekableByteChannel channel, Consumer<ByteBuffer> tracer)
        {
            super(channel, tracer);
        }

        @Override
        public long position() throws IOException
        {
            return ch.position();
        }

        @Override
        public SeekableByteChannel position(long newPosition) throws IOException
        {
            ch.position(newPosition);
            return this;
        }

        @Override
        public long size() throws IOException
        {
            return ch.size();
        }

        @Override
        public SeekableByteChannel truncate(long size) throws IOException
        {
            ch.truncate(size);
            return this;
        }
        
    }
    public static class TraceableByteChannel<T extends ByteChannel> implements ByteChannel
    {
        protected T ch;
        private Consumer<ByteBuffer> tracer;

        public TraceableByteChannel(T channel, Consumer<ByteBuffer> tracer)
        {
            this.ch = channel;
            this.tracer = tracer;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            int before = dst.position();
            int rc = ch.read(dst);
            int after = dst.position();
            dst.position(before);
            tracer.accept(dst);
            dst.position(after);
            return rc;
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            int before = src.position();
            int rc = ch.write(src);
            int after = src.position();
            src.position(before);
            tracer.accept(src);
            src.position(after);
            return rc;
        }
        
        @Override
        public boolean isOpen()
        {
            return ch.isOpen();
        }

        @Override
        public void close() throws IOException
        {
            ch.close();
        }

    }
}
