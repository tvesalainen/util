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
package org.vesalainen.util.concurrent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @deprecated Not used at all
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SynchronizedRingBuffer
{
    private final ByteBuffer bb;
    private final RingbufferSupport bs;
    private RingSpan span;
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore semaphore;
    private int minRead;
    private List<BufferConsumer> consumers = new ArrayList<>();
    private List<Thread> threads = new ArrayList<>();
    private int capacity;

    public SynchronizedRingBuffer(int capacity, boolean direct, int minRead)
    {
        if (minRead > capacity)
        {
            throw new IllegalArgumentException("minRead > capacity");
        }
        if (direct)
        {
            bb = ByteBuffer.allocateDirect(capacity);
        }
        else
        {
            bb = ByteBuffer.allocate(capacity);
        }
        this.capacity = capacity;
        this.minRead = minRead;
        bs = new RingbufferSupport(bb);
        span = new RingSpan(capacity);
        semaphore = new Semaphore(capacity);
    }
    
    public void addConsumer(BufferConsumer consumer)
    {
        checkState();
        lock.lock();
        try
        {
            consumer.setRing(this);
            int count = consumers.size();
            Thread thread = new Thread(consumer, "BufferConsumer-"+count);
            threads.add(thread);
            consumers.add(consumer);
            thread.start();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void read(ScatteringByteChannel channel) throws IOException
    {
        try
        {
            int need = minRead;
            while (true)
            {
                semaphore.acquire(need);
                int rc = (int) channel.read(bs.getBuffers(span.end(), minRead));
                if (rc == -1)
                {
                    semaphore.acquire(capacity-minRead);
                    break;
                }
                need = rc;
                lock.lock();
                try
                {
                    span.increment(rc);
                }
                finally
                {
                    lock.unlock();
                }
                for (BufferConsumer consumer : consumers)
                {
                    consumer.input(rc);
                }
            }
            stopThreads();
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex);
        }
    }
    void release()
    {
        lock.lock();
        try
        {
            int min = Integer.MAX_VALUE;
            for (BufferConsumer consumer : consumers)
            {
                min = Math.min(min, span.length(consumer.position()));
            }
            if (min < 0)
            {
                System.err.println();
            }
            semaphore.release(min);
            span.addStart(min);
        }
        finally
        {
            lock.unlock();
        }
    }

    ByteBuffer getBuffer()
    {
        return bb;
    }
    
    private void checkState()
    {
        if (consumers == null)
        {
            throw new IllegalStateException("stopped");
        }
    }
    public void stopThreads()
    {
        if (threads.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        lock.lock();
        try
        {
            Thread currentThread = Thread.currentThread();
            for (Thread thread : threads)
            {
                if (!currentThread.equals(thread))
                {
                    thread.interrupt();
                }
            }
            threads = null;
            consumers = null;
        }
        finally
        {
            lock.unlock();
        }
    }

}
