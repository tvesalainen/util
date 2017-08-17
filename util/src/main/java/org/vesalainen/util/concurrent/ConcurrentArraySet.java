/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * A class optimized for fast iterator. It's methods are thread-safe.
 * <p>Iterators are not fail-fast. Instead they are using read/write locks.
 * Iterator is write-locked causing other threads to wait. Lock is held until
 * hasNext returns true. Application should call unlock if it exists iterator loop.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConcurrentArraySet<T> implements Set<T>
{
    private final List<T> list = new ArrayList<>();
    private Iter iterator;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReadLock readLock = rwLock.readLock();
    private final WriteLock writeLock = rwLock.writeLock();
    
    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        readLock.lock();
        try
        {
            return list.contains(o);
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Returned iterator is write-locked until hasNext returns false or unlock
     * method is called.
     * @return 
     */
    @Override
    public Iterator<T> iterator()
    {
        writeLock.lock();
        if (iterator == null)
        {
            iterator = new Iter();
        }
        else
        {
            iterator.reset();
        }
        return iterator;
    }
    /**
     * Unlock write-lock locked by iterator. It is safe to call this method 
     * several times. If the current thread is not holding the lock it does nothing.
     */
    public void unlock()
    {
        if (writeLock.isHeldByCurrentThread())
        {
            writeLock.unlock();
        }
    }
    
    @Override
    public Object[] toArray()
    {
        readLock.lock();
        try
        {
            return list.toArray();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        readLock.lock();
        try
        {
            return list.toArray(a);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(T e)
    {
        writeLock.lock();
        try
        {
            if (!list.contains(e))
            {
                list.add(e);
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o)
    {
        writeLock.lock();
        try
        {
            return list.remove(o);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        readLock.lock();
        try
        {
            return list.containsAll(c);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        writeLock.lock();
        try
        {
            boolean changed = false;
            for (T o : c)
            {
                if (add(o))
                {
                    changed = true;
                }
            }
            return changed;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        writeLock.lock();
        try
        {
            return list.retainAll(c);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        writeLock.lock();
        try
        {
            return list.removeAll(c);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void clear()
    {
        writeLock.lock();
        try
        {
            list.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private class Iter implements Iterator<T>
    {
        private int index = size();
        public Iter()
        {
        }

        private void reset()
        {
            index = size();
        }

        @Override
        public boolean hasNext()
        {
            boolean hasNext = index > 0;
            if (!hasNext)
            {
                writeLock.unlock();
            }
            return hasNext;
        }

        @Override
        public T next()
        {
            return list.get(--index);
        }

        @Override
        public void remove()
        {
            list.remove(index);
        }
        
    }
    
}
