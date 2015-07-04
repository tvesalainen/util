/*
 * Copyright (C) 2015 tkv
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

/**
 *
 * @author tkv
 */
public class WeakList<T> implements List<T>
{
    private final List<WeakReference<T>> list = new ArrayList<>();

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
    public Iterator<WeakReference<T>> iterator()
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
    public boolean add(WeakReference<T> e)
    {
        return list.add(e);
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
    public boolean addAll(Collection<? extends WeakReference<T>> c)
    {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends WeakReference<T>> c)
    {
        return list.addAll(index, c);
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
    public void replaceAll(UnaryOperator<WeakReference<T>> operator)
    {
        list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super WeakReference<T>> c)
    {
        list.sort(c);
    }

    @Override
    public void clear()
    {
        list.clear();
    }

    @Override
    public boolean equals(Object o)
    {
        return list.equals(o);
    }

    @Override
    public int hashCode()
    {
        return list.hashCode();
    }

    @Override
    public WeakReference<T> get(int index)
    {
        return list.get(index);
    }

    @Override
    public WeakReference<T> set(int index, WeakReference<T> element)
    {
        return list.set(index, element);
    }

    @Override
    public void add(int index, WeakReference<T> element)
    {
        list.add(index, element);
    }

    @Override
    public WeakReference<T> remove(int index)
    {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<WeakReference<T>> listIterator()
    {
        return list.listIterator();
    }

    @Override
    public ListIterator<WeakReference<T>> listIterator(int index)
    {
        return list.listIterator(index);
    }

    @Override
    public List<WeakReference<T>> subList(int fromIndex, int toIndex)
    {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<WeakReference<T>> spliterator()
    {
        return list.spliterator();
    }
    
}
