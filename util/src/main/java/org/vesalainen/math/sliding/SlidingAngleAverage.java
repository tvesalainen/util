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
 * SlidingAngleAverage calculates average angle.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingAngleAverage extends AbstractSlidingAngleAverage
{
    /**
     * @param size Initial size of buffers
     */
    public SlidingAngleAverage(int size)
    {
        super(size);
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        sin = (double[]) newArray(sin, size, new double[newSize]);
        cos = (double[]) newArray(cos, size, new double[newSize]);
        size = newSize;
    }


    @Override
    protected boolean isRemovable(int index)
    {
        return count() >= size;
    }

}
