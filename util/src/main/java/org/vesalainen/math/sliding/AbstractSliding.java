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
 * Abstract base class for sliding expression calculations. Sliding calculations means 
 * calculating expression for number of last samples or samples that are not older than given time.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSliding
{
    protected int initialSize;
    protected int size;
    protected int margin;
    protected int begin;
    protected int end;
    /**
     * 
     * @param size Initial size of ring buffer
     */
    protected AbstractSliding(int size)
    {
        this.initialSize = size;
        this.size = size;
        this.margin = size/10;
    }
    /**
     * Returns number of free slots before grow. Default is initial size/10.
     * @return 
     */
    public int getMargin()
    {
        return margin;
    }
    /**
     * Sets number of free slots before grow. Default is initial size/10.
     * @param margin 
     */
    public void setMargin(int margin)
    {
        this.margin = margin;
    }

    /**
     * Assign value to inner storage
     * @param index Mod size
     * @param value 
     */
    protected abstract void assign(int index, double value);
    /**
     * Remove item at index
     * @param index Mod index
     */
    protected abstract void remove(int index);
    /**
     * Eliminate values that are no longer used in calculation
     */
    protected void eliminate()
    {
        int count = end-begin;
        while (count > 0 && isRemovable(begin%size))
        {
            remove(begin%size);
            begin++;
            count--;
        }
    }

    /**
     * Called when ring buffer needs more space
     */
    protected abstract void grow();
    /**
     * Returns new size for ringbuffer. This implementations returns 
     * Math.max(begin % size, initialSize) + size;
     * <p>Important! newSize cannot be less than in default implementation.
     * @return 
     */
    protected int newSize()
    {
        return Math.max(begin % size, initialSize) + size;
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
        int sb = begin % oldLen;
        int se = end % oldLen;
        System.arraycopy(old, se, arr, se, oldLen - se);
        System.arraycopy(old, 0, arr, oldLen, sb);
        return arr;
    }

    /**
     * Return true if value at index is not needed anymore.
     * @param index
     * @return
     */
    protected abstract boolean isRemovable(int index);
    
}
