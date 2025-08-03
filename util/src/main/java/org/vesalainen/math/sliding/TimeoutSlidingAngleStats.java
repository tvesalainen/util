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

import java.time.Clock;
import java.util.PrimitiveIterator;
import java.util.function.LongSupplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeoutSlidingAngleStats extends TimeoutSlidingAngleAverage implements TimeoutStats
{
    protected double[] angles;
    protected boolean updated;
    protected double min;
    protected double max;
    
    public TimeoutSlidingAngleStats(int size, long timeout)
    {
        this(System::currentTimeMillis, size, timeout);
    }
    public TimeoutSlidingAngleStats(LongSupplier clock, int initialSize, long timeout)
    {
        super(clock, initialSize, timeout);
        angles = new double[size];
    }

    public double getMax()
    {
        readLock.lock();
        try
        {
            calc();
            return max;
        }
        finally
        {
            readLock.unlock();
        }
    }

    public double getMin()
    {
        readLock.lock();
        try
        {
            calc();
            return min;
        }
        finally
        {
            readLock.unlock();
        }
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
            PrimitiveIterator.OfInt it = modIterator();
            while (it.hasNext())
            {
                double n = (angles[it.nextInt()] + d) % 360;
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

    @Override
    public long lastTime()
    {
        readLock.lock();
        try
        {
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
            }
            return times[(endMod()+size-1) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public long previousTime()
    {
        readLock.lock();
        try
        {
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
            }
            return times[(endMod()+size-2) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }
    
}
