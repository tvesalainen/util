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

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * @deprecated Not used at all
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BufferConsumer implements Runnable
{
    private SynchronizedRingBuffer ring;
    private RingbufferSupport bs;
    private RingSpan span;
    private final Semaphore semaphore = new Semaphore(0);
    /**
     * Reads the next byte
     * @return
     * @throws InterruptedException 
     */
    protected byte read() throws InterruptedException
    {
        semaphore.acquire();
        return bs.get(span.increment());
    }
    /**
     * Releases read input. After that the input is not available.
     */
    protected void release()
    {
        span.clear();
        ring.release();
    }
    RingSpan position()
    {
        return span;
    }
    /**
     * Returns array of buffers that contains ringbuffers content in array of
     * ByteBuffers ready for scattering read of gathering write
     * @return 
     * @see java.nio.channels.ScatteringByteChannel
     * @see java.nio.channels.GatheringByteChannel
     */
    public ByteBuffer[] getInput()
    {
        return bs.getBuffersForSpan(span);
    }
    void setRing(SynchronizedRingBuffer ring)
    {
        if (this.ring != null)
        {
            throw new IllegalStateException("attached already");
        }
        this.ring = ring;
        ByteBuffer buffer = ring.getBuffer();
        bs = new RingbufferSupport(buffer);
        span = new RingSpan(buffer.capacity());
    }
    
    
    protected abstract void consume();
    @Override
    public void run()
    {
        consume();
    }

    void input(int rc)
    {
        semaphore.release(rc);
    }
    
}
