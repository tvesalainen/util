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

import org.vesalainen.math.sliding.SlidingAngleStats;
import org.vesalainen.navi.Navis;

/**
 * AverageSeeker is used to seek average value until values precision is within
 * given range.
 * 
 * <p>This class is thread-safe
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleAverageSeeker extends AbstractSeeker
{
    private AngleAverage average;
    private SlidingAngleStats stats;
    /**
     * Creates AngleAverageSeeker
     * @param windowSize
     * @param action 
     */
    public AngleAverageSeeker(int windowSize)
    {
        this(windowSize, -1, null);
        done = true;
    }
    
    /**
     * Creates AngleAverageSeeker
     * @param windowSize How many last values are checked
     */
    public AngleAverageSeeker(int windowSize, double tolerance, Runnable action)
    {
        super(tolerance, action);
        this.average = new AngleAverage();
        this.stats = new SlidingAngleStats(windowSize);
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
            average.addDeg(value, weight);
            double fast = average.averageDeg();
            stats.accept(fast);
            check();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    /**
     * Returns average angle in degrees
     * @return 
     */
    public double average()
    {
        return average.averageDeg();
    }
    /**
     * Returns maximum deviation.
     * @return 
     */
    public double deviation()
    {
        return deviation(average());
    }
    /**
     * Returns maximum angle clockwise from average
     * @return 
     */
    public double min()
    {
        return stats.getMin();
    }
    /**
     * Returns minimum angle counter clockwise from average
     * @return 
     */
    public double max()
    {
        return stats.getMax();
    }
    /**
     * Returns true if average is within given delta.
     * @param delta
     * @return 
     */
    public boolean isWithin(double delta)
    {
        readLock.lock();
        try
        {
            if (stats.count() >= stats.getInitialSize())
            {
                return (Navis.angleDiff(stats.getMin(), stats.getMax()) < delta);
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
    protected double deviation(double averageDeg)
    {
        return Math.max(Navis.angleDiff(averageDeg, stats.getMax()), Navis.angleDiff(stats.getMin(), averageDeg));
    }
    @Override
    public String toString()
    {
        double averageDeg = average.averageDeg();
        return "AngleAverageSeeker{"  + averageDeg + " ± " + deviation(averageDeg) + '}';
    }
}
