/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.PrimitiveIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSliding
{
    
    protected int initialSize;
    protected int size;
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    protected ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    protected int begin;
    protected int end;

    public AbstractSliding()
    {
    }
    /**
     * Clears buffer(s).
     */
    public void clear()
    {
        end = begin;
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
     * Remove item at index
     * @param index Mod index
     */
    protected abstract void remove(int index);

    /**
     * Eliminate values that are no longer used in calculation
     */
    protected void eliminate()
    {
        int count = end - begin;
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
        return 2 * size;
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
    protected PrimitiveIterator.OfInt modIterator()
    {
        return new ModIter();
    }

    /**
     * Returns iterator for each i%size where i is from end-1 to begin.
     * @return
     */
    protected PrimitiveIterator.OfInt modReverseIterator()
    {
        return new ModRev();
    }

    public int getInitialSize()
    {
        return initialSize;
    }
    
    private class ModIter implements PrimitiveIterator.OfInt
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
    private class ModRev implements PrimitiveIterator.OfInt
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
