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

/**
 * @deprecated Not used at all
 * A support class for ByteBuffer used as ring buffer
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingbufferSupport
{
    private final ByteBuffer bb;
    private final ByteBuffer bb1;
    private final ByteBuffer bb2;
    private final ByteBuffer[] ar0;
    private final ByteBuffer[] ar1;
    private final ByteBuffer[] ar2;
    private final int capacity;
    
    public RingbufferSupport(ByteBuffer bb)
    {
        this.bb = bb;
        capacity = bb.capacity();
        bb1 = bb.duplicate();
        bb2 = bb.duplicate();
        ar0 = new ByteBuffer[] {};
        ar1 = new ByteBuffer[] {bb1};
        ar2 = new ByteBuffer[] {bb1, bb2};
    }
    public byte get(int index)
    {
        return bb.get(index);
    }
    /**
     * Returns array of buffers that contains ringbuffers content in array of
     * ByteBuffers ready for scattering read of gathering write
     * @param start
     * @param len
     * @return 
     * @see java.nio.channels.ScatteringByteChannel
     * @see java.nio.channels.GatheringByteChannel
     */
    public ByteBuffer[] getBuffers(int start, int len)
    {
        if (len == 0)
        {
            return ar0;
        }
        if (len > capacity)
        {
            throw new IllegalArgumentException("len="+len+" > capacity="+capacity);
        }
        int s = start % capacity;
        int e = (start+len) % capacity;
        return getBuffersForSpan(s, e);
    }
    public ByteBuffer[] getBuffersForSpan(RingSpan s)
    {
        return getBuffersForSpan(s.start(), s.end());
    }
    public ByteBuffer[] getBuffersForSpan(int s, int e)
    {
        if (s < e)
        {
            bb1.limit(e);
            bb1.position(s);
            return ar1;
        }
        else
        {
            bb1.limit(capacity);
            bb1.position(s);
            if (e > 0)
            {
                bb2.limit(e);
                bb2.position(0);
                return ar2;
            }
            else
            {
                return ar1;
            }
        }
    }
}
