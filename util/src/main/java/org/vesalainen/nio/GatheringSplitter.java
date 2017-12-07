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
import java.nio.channels.GatheringByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class GatheringSplitter<T> extends Splitter<T>
{
    private final ByteBuffer bb1;
    private final ByteBuffer bb2;
    private final ByteBuffer[] ar2;

    public GatheringSplitter(ByteBuffer buffer)
    {
        super(buffer.capacity());
        bb1 = buffer.duplicate();
        bb2 = buffer.duplicate();
        ar2 = new ByteBuffer[] {bb1, bb2};
    }
    
    @Override
    protected int op(T writer, int position, int limit) throws IOException
    {
        bb1.limit(limit);
        bb1.position(position);
        return (int) write(writer, ar2, 0, 1);
    }

    @Override
    protected int op(T writer, int position1, int limit1, int position2, int limit2) throws IOException
    {
        bb1.limit(limit1);
        bb1.position(position1);
        bb2.limit(limit2);
        bb2.position(position2);
        return (int) write(writer, ar2);
    }

    
    protected abstract int write(T writer, ByteBuffer[] dsts, int offset, int length) throws IOException;
    protected int write(T writer, ByteBuffer[] dsts) throws IOException
    {
        return write(writer, dsts, 0, dsts.length);
    }
}
