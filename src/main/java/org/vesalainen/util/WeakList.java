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
package org.vesalainen.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of List which stores list items in WeakReference
 * 
 * <p>Using indexes to access list items doesn't make any sense, so they are not implemented.
 * @author tkv
 * @param <T>
 * @see java.lang.ref.WeakReference
 */
public class WeakList<T> implements List<T>
{
    private final List<WeakReference<T>> list = new ArrayList<>();
    private boolean gc;

    public WeakList()
    {
    }

    public WeakList(Collection<T> col)
    {
        addAll(col);
    }
    
    @Override
    public int size()
    {
        return (int) stream().count();
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o)
    {
        return stream().anyMatch((x)->{return x.equals(o);});
    }

    @Override
    public Iterator<T> iterator()
    {
        return stream().iterator();
    }

    @Override
    public Object[] toArray()
    {
        return stream().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return stream().toArray((int ii)->{return a;});
    }

    @Override
    public boolean add(T e)
    {
        return list.add(new WeakReference<T>(e));
    }
    /**
     * Returns true if list content has changed because of garbage collection.
     * @return 
     */
    public boolean isGarbageCollected()
    {
        if (gc)
        {
            return gc;
        }
        size();
        return gc;
    }
    
    @Override
    public boolean remove(Object o)
    {
        Iterator<WeakReference<T>> iterator = list.iterator();
        while (iterator.hasNext())
        {
            WeakReference<T> wr = iterator.next();
            T t = wr.get();
            if (t == null)
            {
                iterator.remove();
            }
            else
            {
                if (Objects.equals(t, o))
                {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return stream().allMatch((x)->{return c.contains(x);});
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        for (T t : c)
        {
            add(t);
        }
        return !c.isEmpty();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean res = false;
        for (Object t : c)
        {
            if (remove(t))
            {
                res = true;
            }
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear()
    {
        list.clear();
    }

    @Override
    public T get(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T set(int index, T element)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, T element)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T remove(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int lastIndexOf(Object o)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return new SpliteratorImpl();
    }
    
    private class SpliteratorImpl implements Spliterator<T>
    {
        private final Iterator<WeakReference<T>> iterator = list.iterator();
        
        @Override
        public void forEachRemaining(Consumer<? super T> action)
        {
            while (iterator.hasNext())
            {
                WeakReference<T> wr = iterator.next();
                T t = wr.get();
                if (t == null)
                {
                    gc = true;
                    iterator.remove();
                }
                else
                {
                    action.accept(t);
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action)
        {
            while (iterator.hasNext())
            {
                WeakReference<T> wr = iterator.next();
                T t = wr.get();
                if (t == null)
                {
                    gc = true;
                    iterator.remove();
                }
                else
                {
                    action.accept(t);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return list.size();
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
    
}
