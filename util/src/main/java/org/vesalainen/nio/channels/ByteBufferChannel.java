/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.TimeUnit;
import org.vesalainen.nio.channels.ByteBufferPipe.Sink;
import org.vesalainen.nio.channels.ByteBufferPipe.Source;
import org.vesalainen.nio.channels.vc.VirtualCircuit;

/**
 * ByteBufferChannel is like PipeChannel except that it uses ByteBuffer as 
 * intermediate buffer.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferChannel implements Closeable, AutoCloseable, ByteChannel, GatheringByteChannel, InterruptibleChannel, WritableByteChannel, ReadableByteChannel, ScatteringByteChannel
{
    private Sink sink;
    private Source source;

    private ByteBufferChannel(Sink sink, Source source)
    {
        this.sink = sink;
        this.source = source;
    }
    /**
     * Creates ByteBufferChannel pair with unfair locking policy. Writing to one is readable in another.
     * @param size ByteBuffer size
     * @param direct ByteBuffer direct/heap
     * @return 
     */
    public static ByteBufferChannel[] open(int size, boolean direct)
    {
        return open(size, direct, false);
    }
    /**
     * Creates ByteBufferChannel pair. Writing to one is readable in another.
     * @param size ByteBuffer size
     * @param direct ByteBuffer direct/heap
     * @param fair Locking policy
     * @return 
     */
    public static ByteBufferChannel[] open(int size, boolean direct, boolean fair)
    {
        ByteBufferPipe pipe1 = new ByteBufferPipe(size, direct, fair);
        ByteBufferPipe pipe2 = new ByteBufferPipe(size, direct, fair);
        return new ByteBufferChannel[]{new ByteBufferChannel(pipe1.sink(), pipe2.source()), new ByteBufferChannel(pipe2.sink(), pipe1.source())};
    }
    /**
     * Set time for write method.
     * @param timeout
     * @param unit 
     */
    public void setWriteTimeout(long timeout, TimeUnit unit)
    {
        sink.setTimeout(timeout, unit);
    }
    /**
     * Set timeout for read method.
     * @param timeout
     * @param unit 
     */
    public void setReadTimeout(long timeout, TimeUnit unit)
    {
        source.setTimeout(timeout, unit);
    }
    @Override
    public void close() throws IOException
    {
        sink.close();
        sink = null;
        source.close();
        source = null;
    }

    @Override
    public boolean isOpen()
    {
        return sink != null;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        return sink.write(srcs, offset, length);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        return sink.write(srcs);
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        return sink.write(src);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return source.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return source.read(dsts, offset, length);
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return source.read(dsts);
    }

    private static class BufferedByteBufferChannel extends ByteBufferChannel
    {

        private final VirtualCircuit vc;

        public BufferedByteBufferChannel(Sink sink, Source source, VirtualCircuit vc)
        {
            super(sink, source);
            this.vc = vc;
        }

        @Override
        public void close() throws IOException
        {
            vc.stop();
            super.close();
        }
        
    }
    
}
