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

/**
 * TimeoutSlidingStats is a combined class for average, min, max calculations.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeoutSlidingStats extends TimeoutSlidingAverage implements TimeoutStats
{
    protected TimeoutSlidingMin min;
    protected TimeoutSlidingMax max;
    
    public TimeoutSlidingStats(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    public TimeoutSlidingStats(Clock clock, int initialSize, long timeout)
    {
        super(clock, initialSize, timeout);
        min = new TimeoutSlidingMin(this);
        max = new TimeoutSlidingMax(this);
    }

    @Override
    public void accept(double value)
    {
        writeLock.lock();
        try
        {
            min.accept(value);
            max.accept(value);
            super.accept(value);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void grow()
    {
        super.grow();
        min.grow();
        max.grow();
    }
    
    /**
     * Returns Minimum value
     * @return 
     */
    @Override
    public double getMin()
    {
        return min.getBound();
    }
    /**
     * Returns maximum value
     * @return 
     */
    @Override
    public double getMax()
    {
        return max.getBound();
    }
    
}
