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

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleSlidingAverage extends DoubleAbstractSlidingAverage
{
    /**
     * 
     * @param windowSize Initial size of ring buffer
     */
    public DoubleSlidingAverage(int windowSize)
    {
        super(windowSize);
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return count() >= windowSize;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        size = newSize;
    }
    
}
