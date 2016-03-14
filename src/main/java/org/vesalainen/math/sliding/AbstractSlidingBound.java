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
 * Base class for sliding bound calculation
 * @author tkv
 */
public abstract class AbstractSlidingBound extends AbstractSliding
{
    protected double[] ring;
    /**
     * 
     * @param size Initial size of the ringbuffer
     */
    public AbstractSlidingBound(int size)
    {
        super(size);
        ring = new double[size];
    }
    /**
     * Returns the calculated bound
     * @return 
     */
    public double getBound()
    {
        return ring[begin % size];
    }
    /**
     * Add new value
     * @param value 
     */
    @Override
    public void accept(double value)
    {
        eliminate();
        assign(end % size, value);
        int e = end;
        while (e > begin && exceedsBounds(e, value))
        {
            e--;
            assign(e % size, value);
        }
        end++;
    }
    /**
     * Assign value for inner storage.
     * @param index Mod size
     * @param value 
     */
    @Override
    protected void assign(int index, double value)
    {
        ring[index] = value;
    }
    /**
     * Removes value from calculation
     * @param index Mod size
     */
    @Override
    protected void remove(int index)
    {
    }
    /**
     * return true if value at index is no longer needed.
     * @param index Mod size
     * @return 
     */
    @Override
    protected boolean isRemovable(int index)
    {
        return end-begin >= size;
    }
    /**
     * Grows ring buffer
     */
    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        size = newSize;
    }
    /**
     * Returns true if value exceeds value[index-1]
     * @param index Mod size
     * @param value
     * @return 
     */
    protected abstract boolean exceedsBounds(int index, double value);
    
}
