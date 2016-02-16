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
package org.vesalainen.math;

/**
 * Base class for sliding average calculation
 * @author tkv
 */
public abstract class AbstractSlidingAverage
{
    protected int initialSize;
    protected double[] ring;
    protected int begin;
    protected int end;
    protected double sum;
    /**
     * Creates an AbstractSlidingAverage
     * @param size Size of the ring buffer
     */
    protected AbstractSlidingAverage(int size)
    {
        this.initialSize = size;
        this.ring = new double[size];
    }
    /**
     * Adds new value to sliding average
     * @param value 
     */
    public void add(double value)
    {
        eliminate();
        int count = end-begin;
        if (count >= ring.length)
        {
            grow();
        }
        ring[end%ring.length] = value;
        end++;
        sum += value;
    }
    private void eliminate()
    {
        int count = end-begin;
        while (count > 0 && isRemovable(begin%ring.length))
        {
            sum -= ring[begin%ring.length];
            begin++;
            count--;
        }
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
        int size = ring.length;
        for (int ii=begin;ii<end;ii++)
        {
            s += ring[ii%size];
        }
        return s/(end-begin);
    }
    /**
     * Called when ring buffer needs more space
     */
    protected void grow()
    {
        ring = (double[]) newArray(ring, ring.length, new double[newSize()]);
    }
    protected int newSize()
    {
        int len = ring.length;
        return Math.max(begin%ring.length, initialSize)+len;
    }
    /**
     * Returns new oldLen ring buffer
     * @param old
     * @param oldLen
     * @param arr
     * @return 
     */
    protected Object newArray(Object old, int oldLen, Object arr)
    {
        int sb = begin%oldLen;
        int se = end%oldLen;
        System.arraycopy(old, se, arr, se, oldLen-se);
        System.arraycopy(old, 0, arr, oldLen, sb);
        return arr;
    }
    /**
     * Return true if value at index used anymore.
     * @param index
     * @return 
     */
    protected abstract boolean isRemovable(int index);

}
