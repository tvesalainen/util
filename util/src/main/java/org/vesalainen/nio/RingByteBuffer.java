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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingByteBuffer extends RingBuffer<ByteBuffer,ScatteringByteChannel,GatheringByteChannel>
{
    private SparseByteBufferSplitter splitter;
    /**
     * Creates heap RingByteBuffer with readLimit same as size
     * @param size 
     */
    public RingByteBuffer(int size)
    {
        this(size, false);
    }
    /**
     * Creates RingByteBuffer with readLimit same as size
     * @param size
     * @param direct 
     */
    public RingByteBuffer(int size, boolean direct)
    {
        super(allocate(size, direct));
        splitter = new SparseByteBufferSplitter(buffer);
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
    
    private static ByteBuffer allocate(int size, boolean direct)
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
    public int read(ScatteringByteChannel reader) throws IOException
    {
        return read((dsts, offset, length)->reader.read(dsts, offset, length), splitter);
    }

    @Override
    public int write(GatheringByteChannel writer) throws IOException
    {
        return write((dsts, offset, length)->writer.write(dsts, offset, length), splitter);
    }

    public int read(ByteBuffer bb)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public int write(RingBuffer<ByteBuffer, ScatteringByteChannel, GatheringByteChannel> ring)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString()
    {
        return super.toString()+"->'"+getString()+"'";
    }

}
