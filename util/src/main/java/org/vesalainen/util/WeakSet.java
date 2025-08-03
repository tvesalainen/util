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
package org.vesalainen.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * WeakList based implementation of Set 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class WeakSet<T> implements Set<T>
{
    private WeakList<T> list;
    /**
     * Creates WeakSet using equals for comparison.
     * @see java.util.Objects#equals(java.lang.Object, java.lang.Object) 
     */
    public WeakSet()
    {
        list = new WeakList<>();
    }
    /**
     * Creates WeakSet using given predicate for comparison.
     * @param eq 
     */
    public WeakSet(BiPredicate eq)
    {
        list = new WeakList<>(eq);
    }

    public void lock()
    {
        list.lock();
    }

    public void unlock()
    {
        list.unlock();
    }

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
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        return list.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return list.toArray(a);
    }

    @Override
    public boolean add(T e)
    {
        if (!contains(e))
        {
            list.add(e);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isGarbageCollected()
    {
        return list.isGarbageCollected();
    }

    @Override
    public boolean remove(Object o)
    {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        boolean res = false;
        for (T t : c)
        {
            if (list.add(t))
            {
                res = true;
            }
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return list.retainAll(c);
    }

    @Override
    public void clear()
    {
        list.clear();
    }

    @Override
    public Stream<T> stream()
    {
        return list.stream();
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return list.spliterator();
    }

}
