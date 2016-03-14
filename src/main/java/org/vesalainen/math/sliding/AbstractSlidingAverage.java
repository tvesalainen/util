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

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Base class for sliding average calculation
 * @author tkv
 */
public abstract class AbstractSlidingAverage extends AbstractSliding
{
    protected double[] ring;
    protected double sum;
    /**
     * Creates an AbstractSlidingAverage
     * @param size Initial size of the ring buffer
     */
    protected AbstractSlidingAverage(int size)
    {
        super(size);
        ring = new double[size];
    }
    /**
     * Adds new value to sliding average
     * @param value 
     */
    @Override
    public void accept(double value)
    {
        eliminate();
        int count = end-begin;
        if (count >= size)
        {
            grow();
        }
        assign(end%size, value);
        end++;
    }
            
    @Override
    protected void assign(int index, double value)
    {
        ring[index] = value;
        sum += value;
    }

    @Override
    protected void remove(int index)
    {
        sum -= ring[index];
    }

    /**
     * Returns average without actually calculating cell by cell
     * @return 
     */
    public double fast()
    {
        return sum/(end-begin);
    }

    /**
     * Returns average by calculating cell by cell
     * @return 
     */
    public double average()
    {
        double s = 0;
        for (int ii=begin;ii<end;ii++)
        {
            s += ring[ii%size];
        }
        return s/(end-begin);
    }
    /**
     * Returns values as stream
     * @return 
     */
    public DoubleStream stream()
    {
        if (begin == end)
        {
            return DoubleStream.empty();
        }
        else
        {
            int b = begin % size;
            int e = end % size;
            if (b < e)
            {
                return Arrays.stream(ring, b, e);
            }
            else
            {
                return DoubleStream.concat(Arrays.stream(ring, e, size), Arrays.stream(ring, 0, b));
            }
        }
    }
}
