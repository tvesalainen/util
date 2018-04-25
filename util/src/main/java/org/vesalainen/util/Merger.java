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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Merger
{
    /**
     * Merges argument iteras. Iterators should return values in comp order.
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
        return new IterableImpl(merge(comp, arr));
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
            v1 = i1.next();
            v2 = i2.next();
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
            if (comp.compare(v1, v2) < 0)
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
                v1 = i1.next();
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
                v2 = i2.next();
            }
            else
            {
                v2 = null;
            }
            return tmp;
        }
    }
}
