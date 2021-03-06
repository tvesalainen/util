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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingAngleStats extends SlidingAngleAverage
{
    protected double[] angles;
    protected boolean updated;
    protected double min;
    protected double max;
    
    public SlidingAngleStats(int windowSize)
    {
        super(windowSize);
        angles = new double[size];
    }
    /**
     * Returns maximum angle in degrees
     * @return 
     */
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
    /**
     * Returns minimum angle in degrees
     * @return 
     */
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

}
