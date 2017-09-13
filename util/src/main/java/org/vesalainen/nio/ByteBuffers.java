/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import org.vesalainen.util.ArrayIterator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBuffers
{
      /**
     * Increments position so that position mod align == 0
     * @param bb
     * @param align 
     */
    public static final void align(ByteBuffer bb, int align)
    {
        bb.position(alignedPosition(bb, align));
    }
    /**
     * Returns Incremented position so that position mod align == 0, but doesn't
     * change channels position.
     * @param bb
     * @param align
     * @return 
     */
    public static final int alignedPosition(ByteBuffer bb, int align)
    {
        int position = bb.position();
        int mod = position % align;
        if (mod > 0)
        {
            return position + align - mod;
        }
        else
        {
            return position;
        }
    }
    /**
     * Adds skip to position.
     * @param bb
     * @param skip 
     */
    public static final void skip(ByteBuffer bb, int skip)
    {
        bb.position(bb.position() + skip);
    }

  /**
     * Fills data from position to limit with zeroes.
     * @param bb 
     */
    public static final void clearRemaining(ByteBuffer bb)
    {
        if (bb.hasArray())
        {
            Arrays.fill(bb.array(), bb.arrayOffset()+bb.position(), bb.arrayOffset()+bb.limit(), (byte)0);
        }
        else
        {
            int limit = bb.limit();
            for (int ii=bb.position();ii<limit;ii++)
            {
                bb.put(ii, (byte)0);
            }
        }
    }
    /**
     * Moves bytes from buf to bb as much as is possible. Positions are moved
     * according to move.
     * @param buf
     * @param offset
     * @param length
     * @param bb
     * @return 
     */
    public static final int move(byte[] buf, int offset, int length, ByteBuffer bb)
    {
        int count = Math.min(length, bb.remaining());
        bb.put(buf, offset, count);
        return count;
    }
    /**
     * Moves bytes from bb to buf as much as is possible. Positions are moved
     * according to move.
     * @param bb
     * @param buf
     * @param offset
     * @param length
     * @return 
     */
    public static final int move(ByteBuffer bb, byte[] buf, int offset, int length)
    {
        int count = Math.min(length, bb.remaining());
        bb.get(buf, offset, count);
        return count;
    }
    /**
     * Moves bytes from b1 to b2 as much as is possible. Positions are moved
     * according to move.
     * @param b1
     * @param b2
     * @return 
     */
    public static final long move(ByteBuffer b1, ByteBuffer b2)
    {
        int remaining1 = b1.remaining();
        int remaining2 = b2.remaining();
        if (remaining1 <= remaining2)
        {
            b2.put(b1);
            return remaining1;
        }
        else
        {
            int safeLimit = b1.limit();
            b1.limit(b1.position()+remaining2);
            b2.put(b1);
            b1.limit(safeLimit);
            return remaining2;
        }
    }
    public static final long move(ByteBuffer[] bbArray, int offset, int length, ByteBuffer bb)
    {
        return move(bbArray, offset, length, new ByteBuffer[]{bb}, 0, 1);
    }
    public static final long move(ByteBuffer[] bbArray, ByteBuffer bb)
    {
        return move(bbArray, 0, bbArray.length, new ByteBuffer[]{bb}, 0, 1);
    }
    public static final long move(ByteBuffer bb, ByteBuffer[] bbArray, int offset, int length)
    {
        return move(new ByteBuffer[]{bb}, 0, 1, bbArray, offset, length);
    }
    public static final long move(ByteBuffer bb, ByteBuffer[] bbArray)
    {
        return move(new ByteBuffer[]{bb}, 0, 1, bbArray, 0, bbArray.length);
    }
    public static final long move(ByteBuffer[] bbArray1, ByteBuffer[] bbArray2)
    {
        return move(bbArray1, 0, bbArray2.length, bbArray2, 0, bbArray2.length);
    }
    /**
     * Moves bytes from ba1 to ba2 as much as is possible. Positions are moved
     * according to move.
     * @param bbArray1
     * @param offset1
     * @param length1
     * @param bbArray2
     * @param offset2
     * @param length2
     * @return 
     * @see java.nio.channels.GatheringByteChannel
     * @see java.nio.channels.ScatteringByteChannel
     */
    public static final long move(ByteBuffer[] bbArray1, int offset1, int length1, ByteBuffer[] bbArray2, int offset2, int length2)
    {
        if (length1 == 0 || length2 == 0)
        {
            return 0;
        }
        long res = 0;
        ArrayIterator<ByteBuffer> i1 = new ArrayIterator<>(bbArray1, offset1, length1);
        ArrayIterator<ByteBuffer> i2 = new ArrayIterator<>(bbArray2, offset2, length2);
        ByteBuffer b1 = i1.next();
        ByteBuffer b2 = i2.next();
        while (true)
        {
            res += move(b1, b2);
            if (!b1.hasRemaining())
            {
                if (i1.hasNext())
                {
                    b1 = i1.next();
                }
                else
                {
                    return res;
                }
            }
            if (!b2.hasRemaining())
            {
                if (i2.hasNext())
                {
                    b2 = i2.next();
                }
                else
                {
                    return res;
                }
            }
        }
    }
}
