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
package org.vesalainen.navi;

import org.vesalainen.math.AbstractSlidingAverage;

/**
 * TimeSlidingAverage calculates average for given time.
 * @author tkv
 */
public class TimeSlidingAverage extends AbstractSlidingAverage
{
    private final long time;
    private long[] times;
    /**
     * Creates TimeSlidingAverage
     * @param size Initial size of ring buffer
     * @param millis Average time
     */
    public TimeSlidingAverage(int size, long millis)
    {
        super(size);
        this.time = millis;
        this.times = new long[size];
    }

    @Override
    public void add(double value)
    {
        super.add(value);
        times[(end-1)%times.length] = System.currentTimeMillis();
    }

    
    @Override
    protected boolean isRemovable(int index)
    {
        return System.currentTimeMillis() - times[index] > time;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        super.grow();
        times = (long[]) newArray(times, times.length, new long[newSize]);
    }
    
}
