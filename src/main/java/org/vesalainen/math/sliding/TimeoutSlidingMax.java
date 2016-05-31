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
 * A class for counting max value for given time.
 * @author tkv
 */
public class TimeoutSlidingMax extends AbstractTimeoutSlidingBound implements Max
{
    /**
     * 
     * @param size Initial size of buffers
     * @param timeout Sample timeout in millis 
     */
    public TimeoutSlidingMax(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    public TimeoutSlidingMax(Clock clock, int size, long timeout)
    {
        super(clock, size, timeout);
    }

    public TimeoutSlidingMax(Timeouting parent)
    {
        super(parent);
    }

    @Override
    protected boolean exceedsBounds(int index, double value)
    {
        return ring[(index-1) % size] < ring[index % size];
    }

    @Override
    public double getMax()
    {
        return getBound();
    }
    
   
}
