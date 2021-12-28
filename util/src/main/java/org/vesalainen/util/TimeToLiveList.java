/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import org.vesalainen.math.sliding.TimeValueConsumer;

/**
 * TimeToLiveList items are available just given time.
 * <p>Many of the List methods are not supported!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeToLiveList<T> implements List<T>
{
    private final LongSupplier millis;
    private final long defaultTimeout;
    private final Consumer<T> removeObserver;
    private final ArrList<Wrapper<T>> list = new ArrList<>();

    public TimeToLiveList(long defaultTimeout, TimeUnit unit)
    {
        this(Clock.systemDefaultZone(), defaultTimeout, unit, (x)->{});
    }

    public TimeToLiveList(long defaultTimeout, TimeUnit unit, Consumer<T> removeObserver)
    {
        this(Clock.systemDefaultZone(), defaultTimeout, unit, removeObserver);
    }

    public TimeToLiveList(Clock clock, long defaultTimeout, TimeUnit unit, Consumer<T> removeObserver)
    {
        this(clock::millis, defaultTimeout, unit, removeObserver);
    }

    public TimeToLiveList(LongSupplier millis, long defaultTimeout, TimeUnit unit, Consumer<T> removeObserver)
    {
        this.millis = millis;
        this.defaultTimeout = MILLISECONDS.convert(defaultTimeout, unit);
        this.removeObserver = removeObserver;
    }

    public void forEach(TimeConsumer act)
    {
        list.forEach((w)->act.accept(w.expires-defaultTimeout, w.item));
    }
    
    @Override
    public int size()
    {
        eliminate();
        return list.size();
    }

    @Override
    public boolean isEmpty()
    {
        eliminate();
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        eliminate();
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        eliminate();
        return new Iter(list.iterator());
    }

    @Override
    public Object[] toArray()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(T e)
    {
        return add(millis.getAsLong(), e);
    }
    
    public boolean add(long millis, T e)
    {
        eliminate();
        return list.add(new Wrapper(e, defaultTimeout+millis));
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        boolean res = false;
        for (T t : c)
        {
            boolean added = add(t);
            if (added)
            {
                res = true;
            }
        }
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return list.get(index).item;
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

    private void eliminate()
    {
        long exp = millis.getAsLong();
        int size = list.size();
        for (int ii=0;ii<size;ii++)
        {
            Wrapper<T> w = list.get(ii);
            if (exp < w.expires)
            {
                list.removeRange(0, ii);
                return;
            }
            else
            {
                removeObserver.accept(w.item);
            }
        }
    }

    private static class ArrList<T> extends ArrayList<T>
    {

        public ArrList()
        {
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex)
        {
            super.removeRange(fromIndex, toIndex);
        }
        
    }

    private class Iter implements Iterator<T>
    {
        private Iterator<Wrapper<T>> iterator;
        
        public Iter(Iterator<Wrapper<T>> iterator)
        {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        @Override
        public T next()
        {
            return iterator.next().item;
        }
    }
    private class Wrapper<T>
    {
        private T item;
        private long expires;

        public Wrapper(T item, long expires)
        {
            this.item = item;
            this.expires = expires;
        }
        
    }
    @FunctionalInterface
    public interface TimeConsumer<T>
    {
        void accept(long time, T item);
    }
}
