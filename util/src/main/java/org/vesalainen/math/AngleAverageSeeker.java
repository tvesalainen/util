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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleAverageSeeker
{
    private AngleAverage average;
    private SlidingAngleStats stats;
    /**
     * Creates AverageSeeker
     * @param windowSize How many last values are checked
     */
    public AngleAverageSeeker(int windowSize)
    {
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
        average.addDeg(value, weight);
        double fast = average.averageDeg();
        stats.accept(fast);
    }
    /**
     * Returns true if average is within given delta.
     * @param delta
     * @return 
     */
    public boolean isWithin(double delta)
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
}
