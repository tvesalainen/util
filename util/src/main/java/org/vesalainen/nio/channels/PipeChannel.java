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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PipeChannel implements Closeable, AutoCloseable, Channel, GatheringByteChannel, InterruptibleChannel, WritableByteChannel, ReadableByteChannel, ScatteringByteChannel
{
    private SourceChannel source;
    private SinkChannel sink;

    public PipeChannel(SourceChannel source, SinkChannel sink)
    {
        this.source = source;
        this.sink = sink;
    }

    public static PipeChannel[] createPeers() throws IOException
    {
        PipeChannel[] peers = new PipeChannel[2];
        Pipe pipe1 = Pipe.open();
        Pipe pipe2 = Pipe.open();
        peers[0] = new PipeChannel(pipe1.source(), pipe2.sink());
        peers[1] = new PipeChannel(pipe2.source(), pipe1.sink());
        return peers;
    }
    @Override
    public void close() throws IOException
    {
        source.close();
        sink.close();
    }

    @Override
    public boolean isOpen()
    {
        return source.isOpen() && sink.isOpen();
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
}
