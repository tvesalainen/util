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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AppendableByteChannel implements Appendable
{
    private final WritableByteChannel channel;
    private final ByteBuffer bb;

    public AppendableByteChannel(WritableByteChannel channel, int size, boolean direct)
    {
        this.channel = channel;
        if (direct)
        {
            this.bb = ByteBuffer.allocateDirect(size);
        }
        else
        {
            this.bb = ByteBuffer.allocate(size);
        }
    }
    
    @Override
    public Appendable append(CharSequence csq) throws IOException
    {
        if (csq != null)
        {
            append(csq, 0, csq.length());
        }
        else
        {
            append("null", 0, 4);
        }
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException
    {
        if (csq != null)
        {
            for (int ii=start;ii<end;ii++)
            {
                append(csq.charAt(ii));
            }
        }
        else
        {
            append('n').append('u').append('l').append('l');
        }
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException
    {
        if (!bb.hasRemaining())
        {
            flush();
        }
        bb.put((byte)c);
        return this;
    }
    
    public Appendable flush() throws IOException
    {
        bb.flip();
        channel.write(bb);
        bb.clear();
        return this;
    }
}
