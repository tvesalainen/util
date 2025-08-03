/*
 * Copyright (C) 2012 Timo Vesalainen
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An ArrayList implementation which keeps list items ordered.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class OrderedList<T> extends ArrayList<T> implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final Comparator<T> comp;
    /**
     * Creates OrderedList with natural order
     */
    public OrderedList()
    {
        this((Comparator)null);
    }
    /**
     * Creates OrderedList with natural order
     * @param collection 
     */
    public OrderedList(Collection<? extends T> collection)
    {
        this(collection, null);
    }
    /**
     * Creates OrderedList with comparator or natural order if null comparator
     * @param comp 
     */
    public OrderedList(Comparator<T> comp)
    {
        this.comp = comp;
    }
    /**
     * Creates OrderedList with comparator or natural order if null comparator
     * @param collection
     * @param comp 
     */
    public OrderedList(Collection<? extends T> collection, Comparator<T> comp)
    {
        super(collection);
        this.comp = comp;
        sort(comp);
    }
    /**
     * Returns stream from start to key
     * @param key
     * @param inclusive
     * @param parallel
     * @return 
     */
    public Stream<T> headStream(T key, boolean inclusive, boolean parallel)
    {
        return StreamSupport.stream(headSpliterator(key, inclusive), parallel);
    }
    /**
     * Returns stream from key to end
     * @param key
     * @param inclusive
     * @param parallel
     * @return 
     */
    public Stream<T> tailStream(T key, boolean inclusive, boolean parallel)
    {
        return StreamSupport.stream(tailSpliterator(key, inclusive), parallel);
    }
    /**
     * Returns spliterator from start to key
     * @param key
     * @param inclusive
     * @return 
     */
    public Spliterator<T> headSpliterator(T key, boolean inclusive)
    {
        int point = point(key, !inclusive);
        Iterator<T> iterator = subList(0, point).iterator();
        return Spliterators.spliterator(iterator, size()-point, 0);
    }
    /**
     * Returns spliterator from key to end
     * @param key
     * @param inclusive
     * @return 
     */
    public Spliterator<T> tailSpliterator(T key, boolean inclusive)
    {
        int point = point(key, inclusive);
        Iterator<T> iterator = subList(point, size()).iterator();
        return Spliterators.spliterator(iterator, size()-point, 0);
    }
    /**
     * Returns iterator from start to key
     * @param key
     * @param inclusive
     * @return 
     */
    public Iterator<T> headIterator(T key, boolean inclusive)
    {
        int point = point(key, !inclusive);
        return subList(0, point).iterator();
    }
    /**
     * Returns iterator from key to end
     * @param key
     * @param inclusive
     * @return 
     */
    public Iterator<T> tailIterator(T key, boolean inclusive)
    {
        int point = point(key, inclusive);
        return subList(point, size()).iterator();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        if (isEmpty())
        {
            return -1;
        }
        if (get(0).getClass().isInstance(o))
        {
            T key = (T) o;
            int idx = Collections.binarySearch(this, key, comp);
            if (idx >= 0)
            {
                while (idx < size()-1 && CollectionHelp.compare(key, get(idx+1), comp)==0)
                {
                    idx++;
                }
                return idx;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(Object o)
    {
        if (isEmpty())
        {
            return -1;
        }
        if (get(0).getClass().isInstance(o))
        {
            T key = (T) o;
            int idx = Collections.binarySearch(this, key, comp);
            if (idx >= 0)
            {
                while (idx > 0 && CollectionHelp.compare(key, get(idx-1), comp)==0)
                {
                    idx--;
                }
                return idx;
            }
        }
        return -1;
    }
    
    private int point(T key, boolean inclusive)
    {
        int idx = Collections.binarySearch(this, key, comp);
        if (idx >= 0)
        {
            if (inclusive)
            {
                while (idx > 0 && CollectionHelp.compare(key, get(idx-1), comp)==0)
                {
                    idx--;
                }
            }
            else
            {
                while (idx < size() && CollectionHelp.compare(key, get(idx), comp)==0)
                {
                    idx++;
                }
            }
        }
        else
        {
            idx = -(idx+1);
        }
        return idx;
    }
    @Override
    public boolean add(T e)
    {
        super.add(point(e, true), e);
        return true;
    }

    @Override
    public void add(int index, T element)
    {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        for (T t : c)
        {
            add(t);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException("not supported");
    }

}
