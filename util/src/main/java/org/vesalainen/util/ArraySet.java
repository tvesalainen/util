/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.Predicate;

/**
 * ArraySet is similar to TreeSet. It is backed with ArrayList and it uses
 binary-get for set behavior. ArraySet has extra method get which can
 be used to find item in certain place of array.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <E>
 */
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>
{
    private static final long serialVersionUID = 1L;
    private Comparator<? super E> comparator;
    private List<E> list;

    public ArraySet()
    {
        list = new ArrayList<>();
    }
    
    public ArraySet(Collection<E> collection)
    {
        this();
        addAll(collection);
    }

    public ArraySet(Comparator<? super E> comparator)
    {
        this();
        this.comparator = comparator;
    }
    
    public ArraySet(SortedSet<E> set)
    {
        this();
        this.comparator = set.comparator();
        list.addAll(set);
    }

    public ArraySet(Comparator<? super E> comparator, List<E> list, boolean reverse)
    {
        if (reverse)
        {
            this.comparator = Collections.reverseOrder(comparator);
            this.list = list.subList(0, list.size());
            Collections.reverse(this.list);
        }
        else
        {
            this.comparator = comparator;
            this.list = list;
        }
    }
    /**
     * Returns item if it is found in binary-search
     * @param key
     * @param predicate
     * @return 
     */
    E get(E key)
    {
        int idx = indexOf(key);
        if (idx >= 0)
        {
            return list.get(idx);
        }
        return null;
    }
    /**
     * If an item is found in binary-search, the predicate is tested and either
     * item or null is result.
     * <p>
     * Otherwise item before insertion-point is tested and if passed 
     * returned.
     * <p>
     * Otherwise item at insert-point is tested and if passed returned.
     * <p>
     * If there is no item at insert-point or before it returns null.
     *
     * @param key
     * @param predicate
     * @return 
     */
    public E get(E key, Predicate<E> predicate)
    {
        int idx = indexOf(key);
        if (idx >= 0)
        {
            E e = list.get(idx);
            return predicate.test(e) ? e : null;
        }
        else
        {
            int ip = insertPoint(idx);
            if (ip-1 >= 0)
            {
                E e = list.get(ip-1);
                if (predicate.test(e))
                {
                    return e;
                }
            }
            if (ip < list.size())
            {
                E e = list.get(ip);
                if (predicate.test(e))
                {
                    return e;
                }
            }
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @param o
     * @return 
     */
    @Override
    public boolean remove(Object o)
    {
        int idx = indexOf((E) o);
        if (idx >= 0)
        {
            list.remove(idx);
            return true;
        }
        return false;
    }
    /**
     * {@inheritDoc}
     * @param o
     * @return 
     */
    @Override
    public boolean contains(Object o)
    {
        return indexOf((E) o) >= 0;
    }
    /**
     * {@inheritDoc}
     * @param e
     * @return 
     */
    @Override
    public boolean add(E e)
    {
        return addEntry(e) != null;
    }
    E addEntry(E e)
    {
        int idx = indexOf(e);
        if (idx >= 0)
        {
            return list.get(idx);
        }
        else
        {
            list.add(insertPoint(idx), e);
            return null;
        }
    }
    private int insertPoint(int idx)
    {
        return -idx-1;
    }
    private int indexOf(E e)
    {
        if (comparator != null)
        {
            return Collections.binarySearch(list, e, comparator);
        }
        else
        {
            return Collections.binarySearch(list, e, (Comparator)Comparator.naturalOrder());
        }
    }
    private int lowIndexOf(E e, boolean inclusive)
    {
        int idx = indexOf(e);
        if (idx >= 0)
        {
            return inclusive ? idx : idx+1;
        }
        else
        {
            int ip = insertPoint(idx);
            return inclusive ? ip : ip+1;
        }
    }
    private int highIndexOf(E e, boolean inclusive)
    {
        int idx = indexOf(e);
        if (idx >= 0)
        {
            return inclusive ? idx : idx-1;
        }
        else
        {
            int ip = insertPoint(idx);
            return inclusive ? ip : ip-1;
        }
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Iterator<E> iterator()
    {
        return list.iterator();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public int size()
    {
        return list.size();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Comparator comparator()
    {
        return comparator;
    }
    /**
     * {@inheritDoc}
     * @param fromElement
     * @param toElement
     * @return 
     */
    @Override
    public SortedSet subSet(E fromElement, E toElement)
    {
        return subSet(fromElement, true, toElement, false);
    }
    /**
     * {@inheritDoc}
     * @param toElement
     * @return 
     */
    @Override
    public SortedSet headSet(E toElement)
    {
        return headSet(toElement, false);
    }
    /**
     * {@inheritDoc}
     * @param fromElement
     * @return 
     */
    @Override
    public SortedSet tailSet(E fromElement)
    {
        return tailSet(fromElement, true);
    }
    /**
     * {@inheritDoc}
     * @param fromElement
     * @param fromInclusive
     * @param toElement
     * @param toInclusive
     * @return 
     */
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive)
    {
        int low = lowIndexOf(fromElement, fromInclusive);
        int high = highIndexOf(toElement, toInclusive);
        return new ArraySet(comparator, list.subList(low, high+1), false);
    }
    /**
     * {@inheritDoc}
     * @param toElement
     * @param inclusive
     * @return 
     */
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive)
    {
        int high = highIndexOf(toElement, inclusive);
        return new ArraySet(comparator, list.subList(0, high+1), false);
    }
    /**
     * {@inheritDoc}
     * @param fromElement
     * @param inclusive
     * @return 
     */
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive)
    {
        int low = lowIndexOf(fromElement, inclusive);
        return new ArraySet(comparator, list.subList(low, list.size()), false);
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public E first()
    {
        if (list.isEmpty())
        {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public E last()
    {
        if (list.isEmpty())
        {
            throw new NoSuchElementException();
        }
        return list.get(list.size()-1);
    }
    /**
     * {@inheritDoc}
     * @param e
     * @return 
     */
    @Override
    public E lower(E e)
    {
        int idx = indexOf(e);
        if (idx >= 1)
        {
            return list.get(idx-1);
        }
        else
        {
            int ip = insertPoint(idx);
            if (ip >= 1)
            {
                return list.get(ip-1);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * {@inheritDoc}
     * @param e
     * @return 
     */
    @Override
    public E floor(E e)
    {
        int idx = indexOf(e);
        if (idx >= 1)
        {
            return list.get(idx);
        }
        else
        {
            int ip = insertPoint(idx);
            if (ip >= 1)
            {
                return list.get(ip-1);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * {@inheritDoc}
     * @param e
     * @return 
     */
    @Override
    public E ceiling(E e)
    {
        int idx = indexOf(e);
        if (idx >= 1)
        {
            return list.get(idx);
        }
        else
        {
            int ip = insertPoint(idx);
            if (ip < list.size())
            {
                return list.get(ip);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * {@inheritDoc}
     * @param e
     * @return 
     */
    @Override
    public E higher(E e)
    {
        int idx = indexOf(e);
        if (idx  >= 0)
        {
            if (idx+1 < list.size())
            {
                return list.get(idx+1);
            }
            else
            {
                return null;
            }
        }
        else
        {
            int ip = insertPoint(idx);
            if (ip < list.size())
            {
                return list.get(ip);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public E pollFirst()
    {
        if (!list.isEmpty())
        {
            return list.remove(0);
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public E pollLast()
    {
        if (!list.isEmpty())
        {
            return list.remove(list.size()-1);
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public NavigableSet<E> descendingSet()
    {
        return new ArraySet(comparator, list, true);
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Iterator<E> descendingIterator()
    {
        return descendingSet().iterator();
    }

}
