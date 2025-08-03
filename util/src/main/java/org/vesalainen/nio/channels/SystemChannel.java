/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.channels.Channels;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SystemChannel
{
    public static final GatheringByteChannel err = new OutChannel(System.err);
    public static final GatheringByteChannel out = new OutChannel(System.out);
    public static final ScatteringByteChannel in = new InChannel(System.in);
    
    static class InChannel implements ScatteringByteChannel
    {
        protected InputStream in;
        protected ScatteringByteChannel channel;

        InChannel(InputStream in)
        {
            this.in = in;
            channel = ChannelHelper.newScatteringByteChannel(Channels.newChannel(in));
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
        {
            return channel.read(dsts, offset, length);
        }

        @Override
        public long read(ByteBuffer[] dsts) throws IOException
        {
            return channel.read(dsts);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            return channel.read(dst);
        }

        @Override
        public boolean isOpen()
        {
            return true;
        }

        @Override
        public void close() throws IOException
        {
        }
        
    }
    static class OutChannel implements GatheringByteChannel
    {
        protected OutputStream out;
        protected GatheringByteChannel channel;

        OutChannel(OutputStream out)
        {
            this.out = out;
            channel = ChannelHelper.newGatheringByteChannel(Channels.newChannel(out));
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
        {
            return channel.write(srcs, offset, length);
        }

        @Override
        public long write(ByteBuffer[] srcs) throws IOException
        {
            return channel.write(srcs);
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            return channel.write(src);
        }

        @Override
        public boolean isOpen()
        {
            return true;
        }

        @Override
        public void close() throws IOException
        {
        }

    }
}