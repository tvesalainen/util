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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public abstract class ScatteringSplitter<T> extends Splitter<T>
{
    private final ByteBuffer bb1;
    private final ByteBuffer bb2;
    private final ByteBuffer[] ar2;
    private int readLimit;

    public ScatteringSplitter(ByteBuffer buffer, int readLimit)
    {
        super(buffer.capacity());
        this.readLimit = readLimit;
        bb1 = buffer.duplicate();
        bb2 = buffer.duplicate();
        ar2 = new ByteBuffer[] {bb1, bb2};
    }
    
    @Override
    protected int op(T reader, int position, int limit) throws IOException
    {
        int len = Math.min(readLimit, limit - position);
        bb1.limit(position+len);
        bb1.position(position);
        return (int) read(reader, ar2, 0, 1);
    }

    @Override
    protected int op(T reader, int position1, int limit1, int position2, int limit2) throws IOException
    {
        int len1 = limit1 - position1;
        if (len1 > readLimit)
        {
            int len = Math.min(readLimit, len1);
            bb1.limit(position1+len);
            bb1.position(position1);
            return (int) read(reader, ar2, 0, 1);
        }
        else
        {
            bb1.limit(limit1);
            bb1.position(position1);
            
            int len2 = limit2 - position2;
            int len = Math.min(readLimit-len1, len2);
            bb2.limit(position2+len);
            bb2.position(position2);
            return (int) read(reader, ar2);
        }
    }
    
    protected abstract int read(T reader, ByteBuffer[] dsts, int offset, int length) throws IOException;
    protected int read(T reader, ByteBuffer[] dsts) throws IOException
    {
        return read(reader, dsts, 0, dsts.length);
    }
}
