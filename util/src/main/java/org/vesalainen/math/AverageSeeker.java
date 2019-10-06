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
import org.vesalainen.math.sliding.DoubleSlidingMax;
import org.vesalainen.math.sliding.DoubleSlidingMin;

/**
 * AverageSeeker is used to seek average value until values precision is within
 * given range.
 * 
 * <p>This class is thread-safe
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AverageSeeker extends AbstractSeeker
{
    private SimpleAverage average;
    private DoubleSlidingMin min;
    private DoubleSlidingMax max;
    /**
     * Creates AverageSeeker
     * @param windowSize 
     */
    public AverageSeeker(int windowSize)
    {
        this(windowSize, -1, null);
        done = true;
    }
    /**
     * Creates AverageSeeker
     * @param windowSize How many last values are checked
     * @param tolerance How much min/max cab differ from sliding average
     * @param action Called once when tolerance is reached
     */
    public AverageSeeker(int windowSize, double tolerance, Runnable action)
    {
        super(tolerance, action);
        this.average = new SimpleAverage();
        this.min = new DoubleSlidingMin(windowSize);
        this.max = new DoubleSlidingMax(windowSize);
    }
    /**
     * Add new value with weight
     * @param value
     * @param weight 
     */
    @Override
    public void add(double value, double weight)
    {
        writeLock.lock();
        try
        {
            average.add(value, weight);
            double fast = average.fast();
            min.accept(fast);
            max.accept(fast);
            check();
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
    @Override
    public boolean isWithin(double delta)
    {
        readLock.lock();
        try
        {
            if (average.getCount() > min.getInitialSize())
            {
                return (deviation() < delta);
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
    public double average()
    {
        return average.fast();
    }

    @Override
    public double deviation()
    {
        double fast = average.fast();
        return Math.max(fast-min.getMin(), max.getMax()-fast);
    }

    @Override
    public double max()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double min()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString()
    {
        double fast = average.fast();
        return "AverageSeeker{" + fast + " ± " + Math.max(max.getMax()-fast, fast-min.getMin()) + '}';
    }
    
}
