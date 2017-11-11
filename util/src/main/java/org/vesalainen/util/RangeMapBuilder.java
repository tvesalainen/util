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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class RangeMapBuilder<T>
{
    private static final int LIMIT = 256;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private BinaryMap<Range,T> map = new BinaryMap<>();

    public void putAll(Map<? extends Range,? extends T> map)
    {
        for (Entry<? extends Range, ? extends T> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }
    public void put(Range range, T value)
    {
        for (Range r : map.keySet())
        {
            if (r.intersect(range))
            {
                throw new IllegalArgumentException(r+" and "+range+" intersect");
            }
        }
        map.put(range, value);
        min = Math.min(range.getFrom(), min);
        max = Math.max(range.getTo(), max);
    }
    public RangeMap<T> build()
    {
        if (min == 0 & max == Integer.MAX_VALUE && map.size() == 1)
        {
            return new AllRangeMap(map.values().iterator().next(), map);
        }
        else
        {
            if (max-min < LIMIT)
            {
                return new ArrayRangeMap<>(min, max, map);
            }
            else
            {
                return new BinaryRangeMap<>(map);
            }
        }
    }

    public static class AllRangeMap<T> extends BinaryMap<Range,T>  implements RangeMap<T>
    {
        T single;

        public AllRangeMap(T single, SortedMap<Range, T> map)
        {
            super(map);
            this.single = single;
        }
        
        @Override
        public T get(int n)
        {
            return single;
        }

        @Override
        public Iterator<T> iterator()
        {
            return values().iterator();
        }
        
    }
    public static class ArrayRangeMap<T> extends BinaryMap<Range,T>  implements RangeMap<T>
    {
        private T[] array;
        private int offset;
        public ArrayRangeMap(int min, int max, BinaryMap<Range,T> map)
        {
            super(map);
            this.array = (T[]) new Object[max-min];
            this.offset = min;
            for (Entry<Range,T> entry : map.entrySet())
            {
                int from = entry.getKey().getFrom();
                int to = entry.getKey().getTo();
                T value = entry.getValue();
                for (int ii=from;ii<to;ii++)
                {
                    array[ii-offset] = value;
                }
            }
        }

        @Override
        public T get(int n)
        {
            int idx = n-offset;
            if (idx < 0 || idx >= array.length)
            {
                return null;
            }
            return array[idx];
        }

        @Override
        public Iterator<T> iterator()
        {
            return values().iterator();
        }
        
    }
    public static class BinaryRangeMap<T> extends BinaryMap<Range,T>  implements RangeMap<T>
    {

        public BinaryRangeMap(BinaryMap<Range, T> map)
        {
            super(map);
        }
        
        @Override
        public T get(int n)
        {
            Range key = SimpleRange.getInstance(n);
            Map.Entry<Range, T> entry = get(key, (r,t)->r.accept(n));
            if (entry != null)
            {
                return entry.getValue();
            }
            return null;
        }

        @Override
        public Iterator<T> iterator()
        {
            return values().iterator();
        }
        
    }
}
