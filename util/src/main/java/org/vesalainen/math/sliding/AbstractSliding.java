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

import java.util.Arrays;
import java.util.PrimitiveIterator.OfInt;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.DoubleStream;

/**
 * Abstract base class for sliding expression calculations. Sliding calculations means 
 * calculating expression for number of last samples or samples that are not older than given time.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSliding
{
    protected int initialSize;
    protected int size;
    protected double[] ring;
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected ReadLock readLock = rwLock.readLock();
    protected WriteLock writeLock = rwLock.writeLock();
    private int begin;
    private int end;
    /**
     * 
     * @param initialSize Initial size of ring buffer
     */
    protected AbstractSliding(int initialSize)
    {
        this.initialSize = initialSize;
        if (Integer.bitCount(initialSize) == 1)
        {
            this.size = initialSize;
        }
        else
        {
            this.size = 1<<Integer.highestOneBit(initialSize);
        }
        ring = new double[size];
    }
    /**
     * Returns number of active items. (end-begin)
     * @return 
     */
    public int count()
    {
        readLock.lock();
        try
        {
            return end - begin;
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Returns begin % size
     * @return 
     */
    protected int beginMod()
    {
        return Math.floorMod(begin, size);
    }
    /**
     * return end % size
     * @return 
     */
    protected int endMod()
    {
        return Math.floorMod(end, size);
    }
    /**
     * Increments end by 1
     */
    protected void endIncr()
    {
        end++;
    }
    /**
     * @deprecated Returns -1
     * Returns number of free slots before grow. Default is initial size/10.
     * @return 
     */
    public int getMargin()
    {
        return -1;
    }
    /**
     * @deprecated Does nothing
     * Sets number of free slots before grow. Default is initial size/10.
     * @param margin 
     */
    public void setMargin(int margin)
    {
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
        int mod = Math.floorMod(begin, size);
        while (count > 0 && isRemovable(mod))
        {
            remove(mod);
            begin++;
            count--;
            mod = Math.floorMod(begin, size);
        }
    }

    /**
     * Called when ring buffer needs more space
     */
    protected abstract void grow();
    /**
     * Returns new size for ring buffer. This implementations returns 
     * 2 * size;
     * @return 
     */
    protected final int newSize()
    {
        return 2*size;
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
        int sb = Math.floorMod(begin, oldLen);
        int se = Math.floorMod(end, oldLen);
        if (sb < se)
        {
            System.arraycopy(old, sb, arr, sb, se - sb);
        }
        else
        {
            System.arraycopy(old, sb, arr, sb, oldLen - sb);
            System.arraycopy(old, 0, arr, 0, se);
        }
        return arr;
    }
    /**
     * Copies old arrays contents to arr
     * @param old
     * @param oldLen
     * @param arr 
     * @return  
     */
    protected Object copy(Object old, int oldLen, Object arr)
    {
        int sb = Math.floorMod(begin, oldLen);
        int se = Math.floorMod(end, oldLen);
        if (sb < se)
        {
            System.arraycopy(old, sb, arr, 0, se - sb);
        }
        else
        {
            System.arraycopy(old, sb, arr, 0, oldLen - sb);
            System.arraycopy(old, 0, arr, oldLen - sb, se);
        }
        return arr;
    }

    /**
     * Return true if value at index is not needed anymore.
     * @param index
     * @return
     */
    protected abstract boolean isRemovable(int index);
    /**
     * Returns iterator for each i%size where i is from begin to end-1.
     * @return 
     */
    protected OfInt modIterator()
    {
        return new ModIter();
    }
    /**
     * Returns iterator for each i%size where i is from end-1 to begin.
     * @return 
     */
    protected OfInt modReverseIterator()
    {
        return new ModRev();
    }
    
    /**
     * Returns values as array. Returned array is independent.
     * @return 
     */
    public double[] toArray()
    {
        readLock.lock();
        try
        {
            return (double[]) copy(ring, size, new double[count()]);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public int getInitialSize()
    {
        return initialSize;
    }
    
    /**
     * Returns values as stream in the same order as entered. Stream is valid 
     * only the time that takes to fill margin number of slots.
     * @return 
     */
    public DoubleStream stream()
    {
        return Arrays.stream(toArray());
    }

    private class ModIter implements OfInt
    {
        private int index;

        public ModIter()
        {
            this.index = begin;
        }
        
        @Override
        public boolean hasNext()
        {
            return index != end;
        }
        
        @Override
        public int nextInt()
        {
            return Math.floorMod(index++, size);
        }

    }
    private class ModRev implements OfInt
    {
        private int index;

        public ModRev()
        {
            this.index = end;
        }
        
        @Override
        public boolean hasNext()
        {
            return index != begin;
        }
        
        @Override
        public int nextInt()
        {
            return Math.floorMod(--index, size);
        }

    }
}
