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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.vesalainen.nio.ByteBuffers;
import static org.vesalainen.nio.channels.FilterChannel.BUF_SIZE;
import org.vesalainen.util.function.IOFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterWritableByteChannel extends FilterChannel<WritableByteChannel> implements WritableByteChannel
{
    private OutputStream out;
    private byte[] buf;
    
    public FilterWritableByteChannel(WritableByteChannel channel, IOFunction<? super OutputStream,? extends OutputStream> fout) throws IOException
    {
        super(channel);
        this.out = fout.apply(new Output());
        this.buf = new byte[BUF_SIZE];
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
}
