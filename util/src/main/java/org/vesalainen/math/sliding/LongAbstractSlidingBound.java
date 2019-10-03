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
package org.vesalainen.math.sliding;

import java.util.PrimitiveIterator;
import java.util.function.LongConsumer;

/**
 * Base class for sliding bound calculation
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class LongAbstractSlidingBound extends LongAbstractSliding implements LongConsumer, LongValueArray
{
    protected int windowSize;
    /**
     * 
     * @param initialSize Initial size of the ringbuffer
     */
    public LongAbstractSlidingBound(int initialSize)
    {
        super(initialSize);
        this.windowSize = initialSize;
    }
    /**
     * Returns the calculated bound
     * @return 
     */
    public long getBound()
    {
        readLock.lock();
        try
        {
            return ring[beginMod()];
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Add new value
     * @param value 
     */
    @Override
    public void accept(long value)
    {
        writeLock.lock();
        try
        {
            eliminate();
            endIncr();
            PrimitiveIterator.OfInt rev = modReverseIterator();
            int e = rev.nextInt();
            assign(e, value);
            while (rev.hasNext())
            {
                e = rev.nextInt();
                if (exceedsBounds(e, value))
                {
                    assign(e, value);
                }
                else
                {
                    break;
                }
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    /**
     * Assign value for inner storage.
     * @param index Mod size
     * @param value 
     */
    @Override
    protected void assign(int index, long value)
    {
        ring[index] = value;
    }
    /**
     * Removes value from calculation
     * @param index Mod size
     */
    @Override
    protected void remove(int index)
    {
    }
    /**
     * return true if value at index is no longer needed.
     * @param index Mod size
     * @return 
     */
    @Override
    protected boolean isRemovable(int index)
    {
        return count() >= windowSize;
    }
    /**
     * Grows ring buffer
     */
    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (long[]) newArray(ring, size, new long[newSize]);
        size = newSize;
    }
    /**
     * Returns true if value exceeds value[index-1]
     * @param index Mod size
     * @param value
     * @return 
     */
    protected abstract boolean exceedsBounds(int index, long value);
    
    @Override
    public long last()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return ring[(endMod()+size-1) % size];
    }

    @Override
    public long previous()
    {
        if (count() < 2)
        {
            throw new IllegalStateException("count() < 2");
        }
        return ring[(endMod()+size-2) % size];
    }
    
}
