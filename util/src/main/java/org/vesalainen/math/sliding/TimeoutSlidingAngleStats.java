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

import java.time.Clock;

/**
 *
 * @author tkv
 */
public class TimeoutSlidingAngleStats extends TimeoutSlidingAngleAverage implements TimeoutStats
{
    protected double[] angles;
    protected boolean updated;
    protected double min;
    protected double max;
    
    public TimeoutSlidingAngleStats(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    public TimeoutSlidingAngleStats(Clock clock, int size, long timeout)
    {
        super(clock, size, timeout);
        angles = new double[size];
    }

    @Override
    public double getMax()
    {
        calc();
        return max;
    }

    @Override
    public double getMin()
    {
        calc();
        return min;
    }

    private void calc()
    {
        if (updated)
        {
            updated = false;
            double fast = fast();
            double l = 360;
            double r = 0;
            double d = 360-fast;
            for (int ii=begin;ii<end;ii++)
            {
                double n = (angles[ii % size] + d) % 360;
                if (n <= 180)
                {
                    r = Math.max(r, n);
                }
                else
                {
                    l = Math.min(l, n);
                }
            }
            min = (l + fast) % 360;
            max = (r + fast) % 360;
        }
    }
    @Override
    protected void assign(int index, double value)
    {
        super.assign(index, value);
        angles[index] = value;
        updated = true;
    }

    @Override
    protected void grow()
    {
        int s = size;
        super.grow();
        angles = (double[]) newArray(angles, s, new double[size]);
    }

    public long lastTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(end+size-1) % size];
    }

    public long previousTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(end+size-2) % size];
    }
    
}
