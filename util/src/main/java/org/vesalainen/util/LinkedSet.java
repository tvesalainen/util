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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.SortedSet;

/**
 * A NavigableSet implementation using LinkedList as storage. Element order
 * is in add order.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LinkedSet<T> implements NavigableSet<T>, Serializable
{
    private static final long serialVersionUID = 1L;
    private LinkedList<T> list;

    public LinkedSet()
    {
        this(new LinkedList<T>());
    }

    protected LinkedSet(LinkedList<T> list)
    {
        this.list = list;
    }
    
    @Override
    public T lower(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T floor(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T ceiling(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T higher(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T pollFirst()
    {
        return list.pollFirst();
    }

    @Override
    public T pollLast()
    {
        return list.pollLast();
    }

    @Override
    public Iterator<T> iterator()
    {
        return list.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet()
    {
        LinkedList<T> clone = (LinkedList<T>) list.clone();
        Collections.reverse(clone);
        return new LinkedSet(clone);
    }

    @Override
    public Iterator<T> descendingIterator()
    {
        return list.descendingIterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedSet<T> headSet(T toElement)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedSet<T> tailSet(T fromElement)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comparator<? super T> comparator()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T first()
    {
        return list.getFirst();
    }

    @Override
    public T last()
    {
        return list.getLast();
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
        if (list.contains(e))
        {
            return false;
        }
        else
        {
            list.add(e);
            return true;
        }
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
        boolean changed = false;
        for (T t : c)
        {
            if (add(t))
            {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return list.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return list.removeAll(c);
    }

    @Override
    public void clear()
    {
        list.clear();
    }
    
}
