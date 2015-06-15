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
import java.io.OutputStream;
import java.io.Writer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author tkv
 */
public class ChannelHelper
{
    public static OutputStream getGatheringOutputStream(ByteBuffer[] srcs, int offset, int length)
    {
        return new ByteBufferOutputStream(srcs, offset, length);
    }
    public static GatheringByteChannel getGatheringByteChannel(WritableByteChannel channel)
    {
        return new GatheringByteChannelImpl(channel);
    }
    public static ScatteringByteChannel getScatteringByteChannel(ReadableByteChannel channel)
    {
        return new ScatteringByteChannelImpl(channel);
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