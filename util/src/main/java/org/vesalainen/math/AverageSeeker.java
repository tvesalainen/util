/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.vesalainen.math.sliding.SlidingMax;
import org.vesalainen.math.sliding.SlidingMin;

/**
 * AverageSeeker is used to seek average value until values precision is within
 * given range.
 * 
 * <p>This class is thread-safe
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AverageSeeker
{
    private SimpleAverage average;
    private SlidingMin min;
    private SlidingMax max;
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    protected ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    /**
     * Creates AverageSeeker
     * @param windowSize How many last values are checked
     */
    public AverageSeeker(int windowSize)
    {
        this.average = new SimpleAverage();
        this.min = new SlidingMin(windowSize);
        this.max = new SlidingMax(windowSize);
    }
    /**
     * Add new value with 1.0 weight
     * @param value 
     */
    public void add(double value)
    {
        add(value, 1.0);
    }
    /**
     * Add new value with weight
     * @param value
     * @param weight 
     */
    public void add(double value, double weight)
    {
        writeLock.lock();
        try
        {
            average.add(value, weight);
            double fast = average.fast();
            min.accept(fast);
            max.accept(fast);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    /**
     * Returns true if average is within given delta.
     * @return 
     */
    public boolean isWithin(double delta)
    {
        readLock.lock();
        try
        {
            if (average.getCount() > min.getInitialSize())
            {
                return (max.getMax() - min.getMin() < delta);
            }
            else
            {
                return false;
            }
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        double fast = average.fast();
        return "AverageSeeker{" + fast + " ± " + Math.max(max.getMax()-fast, fast-min.getMin()) + '}';
    }
    
}
