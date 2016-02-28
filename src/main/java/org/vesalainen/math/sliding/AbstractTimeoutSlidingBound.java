/*
 * Copyright (C) 2016 tkv
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

/**
 * Base class for timeout sliding bound calculation. Each sample has given timeout.
 * @author tkv
 */
public abstract class AbstractTimeoutSlidingBound extends AbstractSlidingBound
{
    protected final long timeout;
    protected long[] times;
    /**
     * 
     * @param size Initial ringbuffer size
     * @param timeout Sample timeout in millis.
     */
    protected AbstractTimeoutSlidingBound(int size, long timeout)
    {
        super(size);
        this.timeout = timeout;
        this.times = new long[size];
    }
    /**
     * Add new value
     * @param value 
     */
    @Override
    public void add(double value)
    {
        eliminate();
        if (end-begin >= size)
        {
            grow();
        }
        assign(end % size, value);
        while (end > begin && exceedsBounds(end, value))
        {
            end--;
            assign(end % size, value);
        }
        end++;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    @Override
    protected void assign(int index, double value)
    {
        ring[index] = value;
        times[index] = System.currentTimeMillis();
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return System.currentTimeMillis() - times[index] > timeout;
    }

}
