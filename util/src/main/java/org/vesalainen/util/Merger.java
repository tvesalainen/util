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
package org.vesalainen.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Merger
{
    /**
     * Merges argument iterators. Iterators should return values in natural order.
     * @param <T>
     * @param iterables
     * @return 
     */
    public static <T> Iterable<T> merge(Iterable<T>... iterables)
    {
        return merge(null, iterables);
    }
    /**
     * Merges argument iterators. Iterators should return values in comp order.
     * @param <T>
     * @param comp
     * @param iterables
     * @return 
     */
    public static <T> Iterable<T> merge(Comparator<T> comp, Iterable<T>... iterables)
    {
        Iterator[] arr = new Iterator[iterables.length];
        for (int ii=0;ii<arr.length;ii++)
        {
            arr[ii] = iterables[ii].iterator();
        }
        return iterable(merge(comp, arr));
    }
    /**
     * Merges argument iterators. Iterators should return values in natural order.
     * @param <T>
     * @param iterators
     * @return 
     */
    public static <T> Iterator<T> merge(Iterator<T>... iterators)
    {
        return merge(null, iterators);
    }
    /**
     * Merges argument iterators. Iterators should return values in comp order.
     * @param <T>
     * @param comp
     * @param iterators
     * @return 
     */
    public static <T> Iterator<T> merge(Comparator<T> comp, Iterator<T>... iterators)
    {
        switch (iterators.length)
        {
            case 0:
                throw new IllegalArgumentException("no iterables");
            case 1:
                return iterators[0];
            case 2:
                return new IteratorImpl<>(comp, iterators[0], iterators[1]);
            default:
                return new IteratorImpl<>(comp, iterators[0], merge(comp, Arrays.copyOfRange(iterators, 1, iterators.length)));
        }
    }
    public static <T> Iterable<T> iterable(Iterator<T> iterator)
    {
        return new IterableImpl(iterator);
    }
    private static class IterableImpl<T> implements Iterable<T>
    {
        private Iterator<T> iterator;

        public IterableImpl(Iterator<T> iterator)
        {
            this.iterator = iterator;
        }

        @Override
        public Iterator<T> iterator()
        {
            return iterator;
        }
        
    }
    /**
     * Returns Queue as iterator. Calls isEmpty and remove methods
     * @param <T>
     * @param queue
     * @return 
     */
    public static <T> Iterator<T> iterator(Queue<T> queue)
    {
        return new XIteratorImpl<>(()->!queue.isEmpty(), queue::remove);
    }
    /**
     * Returns Queue as iterator. Calls poll method
     * @param <T>
     * @param queue
     * @return 
     */
    public static <T> Iterator<T> iterator(BlockingQueue<T> queue)
    {
        return iterator(queue, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * Returns Queue as iterator. Calls poll method. When timeout throws 
     * NoSuchElementException which will remove it from merger.
     * @param <T>
     * @param queue
     * @param time
     * @param unit
     * @return 
     */
    public static <T> Iterator<T> iterator(BlockingQueue<T> queue, long time, TimeUnit unit)
    {
        return new XIteratorImpl<>(()->true, ()->
        {
            try
            {
                T item = queue.poll(time, unit);
                if (item == null)
                {
                    throw new NoSuchElementException("timeout");
                }
                return item;
            }
            catch (InterruptedException ex)
            {
                throw new NoSuchElementException("interrupted");
            }
        });
    }
    private static class XIteratorImpl<T> implements Iterator<T>
    {
        private BooleanSupplier hasNext;
        private Supplier<T> next;

        public XIteratorImpl(BooleanSupplier hasNext, Supplier<T> next)
        {
            this.hasNext = hasNext;
            this.next = next;
        }

        @Override
        public boolean hasNext()
        {
            return hasNext.getAsBoolean();
        }

        @Override
        public T next()
        {
            return next.get();
        }
        
    }
    private static class IteratorImpl<T> implements Iterator<T>
    {
        private final Comparator<T> comp;
        private final Iterator<T> i1;
        private final Iterator<T> i2;
        private T v1;
        private T v2;
        
        public IteratorImpl(Comparator<T> comp, Iterator<T> i1, Iterator<T> i2)
        {
            this.comp = comp;
            this.i1 = i1;
            this.i2 = i2;
            if (i1.hasNext())
            {
                v1 = i1.next();
            }
            if (i2.hasNext())
            {
                v2 = i2.next();
            }
        }

        @Override
        public boolean hasNext()
        {
            return v1 != null || v2 != null;
        }

        @Override
        public T next()
        {
            if (v1 == null)
            {
                return next2();
            }
            if (v2 == null)
            {
                return next1();
            }
            if (CollectionHelp.compare(v1, v2, comp) < 0)
            {
                return next1();
            }
            else
            {
                return next2();
            }
        }
        private T next1()
        {
            T tmp = v1;
            if (i1.hasNext())
            {
                try
                {
                    v1 = i1.next();
                }
                catch (NoSuchElementException ex)
                {
                    v1 = null;
                }
            }
            else
            {
                v1 = null;
            }
            return tmp;
        }
        private T next2()
        {
            T tmp = v2;
            if (i2.hasNext())
            {
                try
                {
                    v2 = i2.next();
                }
                catch (NoSuchElementException ex)
                {
                    v2 = null;
                }
            }
            else
            {
                v2 = null;
            }
            return tmp;
        }
    }
}
