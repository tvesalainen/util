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
 *
 * @author tkv
 */
public abstract class AbstractSlidingAverage
{
    protected int initialSize;
    protected double[] ring;
    protected int begin;
    protected int end;
    protected double sum;

    protected AbstractSlidingAverage(int size)
    {
        this.initialSize = size;
        this.ring = new double[size];
    }
    
    public void add(double value)
    {
        int count = end-begin;
        while (count > 0 && isRemovable(begin%ring.length))
        {
            sum -= ring[begin%ring.length];
            begin++;
            count--;
        }
        if (count >= ring.length)
        {
            grow();
        }
        ring[end%ring.length] = value;
        end++;
        sum += value;
    }
    /**
     * Returns average without actually calculating cell by cell
     * @return 
     */
    public double fast()
    {
        return sum/(end-begin);
    }
    
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
    
    private void grow()
    {
        int sb = begin%ring.length;
        int se = end%ring.length;
        int add = Math.max(sb, initialSize);
        double[] old = ring;
        ring = new double[ring.length+add];
        System.arraycopy(old, se, ring, se, old.length-se);
        System.arraycopy(old, 0, ring, old.length, sb);
    }

    protected abstract boolean isRemovable(int index);
}
