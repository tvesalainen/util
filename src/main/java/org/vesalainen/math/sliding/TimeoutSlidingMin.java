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

/**
 * A class for counting min value for given time.
 * @author tkv
 */
public class TimeoutSlidingMin extends AbstractTimeoutSlidingBound
{
    /**
     * 
     * @param size Initial size of buffers
     * @param timeout Sample timeout in millis 
     */
    public TimeoutSlidingMin(int size, long timeout)
    {
        super(size, timeout);
    }
    
    @Override
    protected boolean exceedsBounds(int index, double value)
    {
        return ring[(index-1) % size] > ring[index % size];
    }
    
   
}