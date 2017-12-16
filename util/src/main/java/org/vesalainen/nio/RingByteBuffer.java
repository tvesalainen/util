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
 * RingByteBuffer is a ByteBuffer implementation of RingBuffer
 * <p>This class is not thread-safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingByteBuffer extends RingBuffer<ByteBuffer,ScatteringByteChannel,GatheringByteChannel>
{
    private SparseByteBufferSplitter readSplitter;
    private SparseByteBufferSplitter writeSplitter;
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
        readSplitter = new SparseByteBufferSplitter(buffer);
        writeSplitter = new SparseByteBufferSplitter(buffer);
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
        return read((dsts, offset, length)->reader.read(dsts, offset, length), readSplitter);
    }

    @Override
    public int write(GatheringByteChannel writer) throws IOException
    {
        return write((srcs, offset, length)->writer.write(srcs, offset, length), writeSplitter);
    }
    public int read(ByteBuffer[] bbs, int offset, int length) throws IOException
    {
        int count = 0;
        for (int ii=offset;ii<length;ii++)
        {
            count += read(bbs[ii]);
        }
        return count;
    }
    public int write(ByteBuffer[] bbs, int offset, int length) throws IOException
    {
        int count = 0;
        for (int ii=offset;ii<length;ii++)
        {
            count += write(bbs[ii]);
        }
        return count;
    }
    /**
     * Reads bytes from bb as much as there is room in this buffer starting from 
     * limit and not passing mark/position. Positions are updated in bb and also
     * in this buffer.
     * @param bb
     * @return
     * @throws IOException 
     */
    public int read(ByteBuffer bb) throws IOException
    {
        return read((dsts, offset, length)->ByteBuffers.move(bb, dsts, offset, length), readSplitter);
    }
    /**
     * Writes bytes from mark (included) to position (excluded) to bb as much as
     * bb has remaining. bb positions are updated.
     * @param bb
     * @return
     * @throws IOException 
     */
    public int write(ByteBuffer bb) throws IOException
    {
        return write((srcs, offset, length)->ByteBuffers.move(srcs, offset, length, bb), readSplitter);
    }
    /**
     * Write this buffers content from mark (included) to position (excluded). 
     * Returns count of actual written items.
     * @param ring
     * @return 
     * @throws java.io.IOException 
     */
    public int write(RingByteBuffer ring) throws IOException
    {
        return write((srcs, offset, length)->ring.read(srcs, offset, length), readSplitter);
    }

    @Override
    public String toString()
    {
        return super.toString()+"->'"+getString()+"'";
    }

}
