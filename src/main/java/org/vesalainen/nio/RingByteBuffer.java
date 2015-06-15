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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * 
 * @author tkv
 */
public class RingByteBuffer extends RingBuffer<ByteBuffer,ScatteringByteChannel,GatheringByteChannel>
{
    private final ByteBuffer bb1;
    private final ByteBuffer bb2;
    private final ByteBuffer[] ar2;
    
    public RingByteBuffer(int size)
    {
        this(size, false);
    }
    public RingByteBuffer(int size, boolean direct)
    {
        super(size, direct);
        bb1 = buffer.duplicate();
        bb2 = buffer.duplicate();
        ar2 = new ByteBuffer[] {bb1, bb2};
    }
    /**
     * Returns byte at current position and increments the position.
     * @return 
     */
    public byte get()
    {
        return get(false);
    }
    /**
     * Returns byte at current position and increments the position. If mark == true
     * the current position is marked.
     * @param mark
     * @return 
     */
    public byte get(boolean mark)
    {
        return (byte) (buffer.get(rawGet(mark)) & 0xff);
    }

    @Override
    public int implGetAt(int index)
    {
        return buffer.get((mark+index)%capacity) & 0xff;
    }
    
    @Override
    protected ByteBuffer allocate(int size, boolean direct)
    {
        if (direct)
        {
            return ByteBuffer.allocateDirect(size);
        }
        else
        {
            return ByteBuffer.allocate(size);
        }
    }

    @Override
    protected int read(ScatteringByteChannel reader, int position, int limit) throws IOException
    {
        bb1.limit(limit);
        bb1.position(position);
        return (int) reader.read(ar2, 0, 1);
    }

    @Override
    protected int read(ScatteringByteChannel reader, int position1, int limit1, int position2, int limit2) throws IOException
    {
        bb1.limit(limit1);
        bb1.position(position1);
        bb2.limit(limit2);
        bb2.position(position2);
        return (int) reader.read(ar2);
    }

    @Override
    protected int write(GatheringByteChannel writer, int position, int limit) throws IOException
    {
        bb1.limit(limit);
        bb1.position(position);
        return (int) writer.write(ar2, 0, 1);
    }

    @Override
    protected int write(GatheringByteChannel writer, int position1, int limit1, int position2, int limit2) throws IOException
    {
        bb1.limit(limit1);
        bb1.position(position1);
        bb2.limit(limit2);
        bb2.position(position2);
        return (int) writer.write(ar2);
    }

    @Override
    public String toString()
    {
        return super.toString()+"->'"+getString()+"'";
    }

}