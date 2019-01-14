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

import org.vesalainen.math.sliding.SlidingMax;
import org.vesalainen.math.sliding.SlidingMin;

/**
 * AverageSeeker is used to seek average value until values precision is within
 * given range.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AverageSeeker
{
    private SimpleAverage average;
    private SlidingMin min;
    private SlidingMax max;
    /**
     * Creates AverageSeeker
     * @param windowSize How many last values are checked
     * @param delta Last window size values must differ less than delta from average
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
        average.add(value, weight);
        double fast = average.fast();
        min.accept(fast);
        max.accept(fast);
    }
    /**
     * Returns true if average is within given delta.
     * @return 
     */
    public boolean isWithin(double delta)
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
}
