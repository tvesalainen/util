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
 * TimeoutSlidingAverage calculates average for given timeout.
 * @author tkv
 */
public class TimeoutSlidingAverage extends AbstractSlidingAverage
{
    protected final long timeout;
    protected double[] ring;
    protected long[] times;
    protected double sum;
    /**
     * Creates TimeoutSlidingAverage
     * @param size Initial size of ring buffer
     * @param timeout Sample timeout
     */
    public TimeoutSlidingAverage(int size, long timeout)
    {
        super(size);
        this.timeout = timeout;
        this.ring = new double[size];
        this.times = new long[size];
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return System.currentTimeMillis() - times[index] > timeout;
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
        sum += value;
        times[index] = System.currentTimeMillis();
    }

    @Override
    protected void remove(int index)
    {
        sum -= ring[index];
    }

    @Override
    public double fast()
    {
        return sum/(end-begin);
    }

    @Override
    public double average()
    {
        double s = 0;
        for (int ii=begin;ii<end;ii++)
        {
            s += ring[ii%size];
        }
        return s/(end-begin);
    }

}
