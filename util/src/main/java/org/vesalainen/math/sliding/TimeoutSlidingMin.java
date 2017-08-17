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
 * A class for counting min value for given time.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeoutSlidingMin extends AbstractTimeoutSlidingBound implements Min
{
    /**
     * 
     * @param size Initial size of buffers
     * @param timeout Sample timeout in millis 
     */
    public TimeoutSlidingMin(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    public TimeoutSlidingMin(Clock clock, int size, long timeout)
    {
        super(clock, size, timeout);
    }

    public TimeoutSlidingMin(Timeouting parent)
    {
        super(parent);
    }

    @Override
    protected boolean exceedsBounds(int index, double value)
    {
        return ring[(index-1) % size] > ring[index % size];
    }

    @Override
    public double getMin()
    {
        return getBound();
    }
    
   
}
