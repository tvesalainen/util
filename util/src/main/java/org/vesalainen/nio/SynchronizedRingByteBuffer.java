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
import java.nio.channels.ScatteringByteChannel;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.util.concurrent.PredicateSynchronizer;

/**
 * A ring-byte-buffer that synchronizes fills and writeTos/gets so that only one 
 * thread can call fill and only one thread can call writeTo/get method. It is
 * allowed for one thread call fill and another call writeTo/get at the same time.
 * An another way to say it is fills have one lock and writeTo/get methods have
 * another lock.
 * <p>
 * It is possible to transfer data synchronously between two threads. Sending 
 * thread calls fillAll/tryFillAll and receiving calls syncWriteTo.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SynchronizedRingByteBuffer extends RingByteBuffer
{
    private ReentrantLock fillLock = new ReentrantLock();   // limit
    private ReentrantLock writeToLock = new ReentrantLock();  // writeSplitter
    private PredicateSynchronizer fillSync = new PredicateSynchronizer();
    private PredicateSynchronizer getSync = new PredicateSynchronizer();
    
    public SynchronizedRingByteBuffer(int size)
    {
        super(size);
    }

    public SynchronizedRingByteBuffer(int size, boolean direct)
    {
        super(size, direct);
    }
    /**
     * Method waits until there's remaining. Calls getAll(false), writeTo and discard.
     * @param ring
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public int syncWriteTo(RingByteBuffer ring) throws IOException, InterruptedException
    {
        writeToLock.lock();
        try
        {
            getSync.waitUntil(()->hasRemaining());
            getAll(false);
            int rc = super.writeTo(ring);
            discard();
            return rc;
        }
        finally
        {
            writeToLock.unlock();
        }
    }
    @Override
    public int writeTo(RingByteBuffer ring) throws IOException
    {
        writeToLock.lock();
        try
        {
            return super.writeTo(ring);
        }
        finally
        {
            writeToLock.unlock();
        }
    }

    @Override
    public int writeTo(ByteBuffer bb) throws IOException
    {
        writeToLock.lock();
        try
        {
            return super.writeTo(bb);
        }
        finally
        {
            writeToLock.unlock();
        }
    }

    @Override
    public int writeTo(ByteBuffer[] bbs, int offset, int length) throws IOException
    {
        writeToLock.lock();
        try
        {
            return super.writeTo(bbs, offset, length);
        }
        finally
        {
            writeToLock.unlock();
        }
    }

    @Override
    public int writeTo(GatheringByteChannel writer) throws IOException
    {
        writeToLock.lock();
        try
        {
            return super.writeTo(writer);
        }
        finally
        {
            writeToLock.unlock();
        }
    }
    /**
     * Calls fill if rings marked fits. Otherwise returns 0.
     * @param ring
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public int tryFillAll(RingByteBuffer ring) throws IOException, InterruptedException
    {
        fillLock.lock();
        try
        {
            if (ring.marked() > free())
            {
                return 0;
            }
            return fill(ring);
        }
        finally
        {
            fillLock.unlock();
        }
    }
    /**
     * Waits until ring.marked fits.
     * @param ring
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public int fillAll(RingByteBuffer ring) throws IOException, InterruptedException
    {
        if (ring.marked() > capacity)
        {
            throw new IllegalArgumentException("marked > capacity");
        }
        fillLock.lock();
        try
        {
            fillSync.waitUntil(()->ring.marked()<=free());
            return fill(ring);
        }
        finally
        {
            fillLock.unlock();
        }
    }
    @Override
    public int fill(RingByteBuffer ring) throws IOException
    {
        fillLock.lock();
        try
        {
            return super.fill(ring);
        }
        finally
        {
            getSync.update();
            fillLock.unlock();
        }
    }

    @Override
    public int fill(ByteBuffer bb) throws IOException
    {
        fillLock.lock();
        try
        {
            return super.fill(bb);
        }
        finally
        {
            getSync.update();
            fillLock.unlock();
        }
    }

    @Override
    public int fill(ByteBuffer[] bbs, int offset, int length) throws IOException
    {
        fillLock.lock();
        try
        {
            return super.fill(bbs, offset, length);
        }
        finally
        {
            getSync.update();
            fillLock.unlock();
        }
    }

    @Override
    public int fill(ScatteringByteChannel reader) throws IOException
    {
        fillLock.lock();
        try
        {
            return super.fill(reader);
        }
        finally
        {
            getSync.update();
            fillLock.unlock();
        }
    }

    public void waitRemaining() throws InterruptedException
    {
        getSync.waitUntil(()->hasRemaining());
    }
    @Override
    public void discard()
    {
        writeToLock.lock();
        try
        {
            super.discard();
        }
        finally
        {
            fillSync.update();
            writeToLock.unlock();
        }
    }

    @Override
    public byte get(boolean mark)
    {
        writeToLock.lock();
        try
        {
            return super.get(mark);
        }
        finally
        {
            if (mark)
            {
                fillSync.update();
            }
            writeToLock.unlock();
        }
    }

    @Override
    public void getAll(boolean markIt)
    {
        writeToLock.lock();
        try
        {
            super.getAll(markIt);
        }
        finally
        {
            if (markIt)
            {
                fillSync.update();
            }
            writeToLock.unlock();
        }
    }
    
}
