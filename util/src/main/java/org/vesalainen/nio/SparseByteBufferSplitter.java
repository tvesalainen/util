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
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SparseByteBufferSplitter extends Splitter<SparseBufferOperator<ByteBuffer>>
{
    private final ByteBuffer bb1;
    private final ByteBuffer bb2;
    private final ByteBuffer[] ar2;
    private final ReentrantLock lock;

    public SparseByteBufferSplitter(ByteBuffer buffer)
    {
        super(buffer.capacity());
        bb1 = buffer.duplicate();
        bb2 = buffer.duplicate();
        ar2 = new ByteBuffer[] {bb1, bb2};
        lock = new ReentrantLock();
    }
    /**
     * Locks this splitters lock
     */
    public void lock()
    {
        lock.lock();
    }
    /**
     * Unlocks this splitters lock
     */
    public void unlock()
    {
        lock.unlock();
    }
    /**
     * Calls super class split locked.
     * @param obj
     * @param start
     * @param length
     * @return
     * @throws IOException 
     */
    @Override
    public int split(SparseBufferOperator<ByteBuffer> obj, int start, int length) throws IOException
    {
        lock.lock();
        try
        {
            return super.split(obj, start, length);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    @Override
    protected int op(SparseBufferOperator operator, int position, int limit) throws IOException
    {
        bb1.limit(limit);
        bb1.position(position);
        return (int) operator.apply(ar2, 0, 1);
    }

    @Override
    protected int op(SparseBufferOperator operator, int position1, int limit1, int position2, int limit2) throws IOException
    {
        bb1.limit(limit1);
        bb1.position(position1);
        bb2.limit(limit2);
        bb2.position(position2);
        return (int) operator.apply(ar2);
    }
    
}
