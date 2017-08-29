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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.function.IOFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterReadableByteChannel extends FilterChannel<ReadableByteChannel> implements ReadableByteChannel
{
    private InputStream in;
    private byte[] buf;
    private int offset;
    private int length;

    public FilterReadableByteChannel(ReadableByteChannel channel, IOFunction<? super InputStream,? extends InputStream> fin) throws IOException
    {
        super(channel);
        this.in = fin.apply(new Input());
        this.buf = new byte[BUF_SIZE];
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
        return count;
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
