/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferInputStream extends InputStream
{
    private ByteBuffer bb;

    public ByteBufferInputStream(ByteBuffer bb)
    {
        this.bb = bb;
    }

    @Override
    public int read() throws IOException
    {
        if (bb.hasRemaining())
        {
            return bb.get();
        }
        return -1;
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException
    {
        bb.reset();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        bb.mark();
    }

    @Override
    public int available() throws IOException
    {
        return bb.remaining();
    }

    @Override
    public long skip(long n) throws IOException
    {
        long skip = 0;
        if (n >= 0)
        {
            skip = Math.min(bb.remaining(), n);
        }
        else
        {
            skip = Math.max(-bb.position(), n);
        }
        bb.position((int) (bb.position()+skip));
        return skip;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (bb.hasRemaining())
        {
            return ByteBuffers.move(bb, b, off, len);
        }
        return -1;
    }
    
}
