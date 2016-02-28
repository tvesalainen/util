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

import org.vesalainen.math.sliding.AbstractSlidingAngleAverage;

/**
 * TimeSlidingAverage calculates average for given time.
 * @author tkv
 */
public class TimeSlidingAngleAverage extends AbstractSlidingAngleAverage
{
    protected final long time;
    protected long[] times;
    /**
     * Creates TimeSlidingAngleAverage
     * @param size Initial size of buffers
     * @param millis Average time
     */
    public TimeSlidingAngleAverage(int size, long millis)
    {
        super(size);
        this.time = millis;
        this.times = new long[size];
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
        sin = (double[]) newArray(sin, size, new double[newSize]);
        cos = (double[]) newArray(cos, size, new double[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    @Override
    protected void assign(int index, double value)
    {
        super.assign(index, value);
        times[index] = System.currentTimeMillis();
    }

}
