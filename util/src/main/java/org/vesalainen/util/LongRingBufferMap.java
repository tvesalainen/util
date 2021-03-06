/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ObjLongConsumer;

/**
 * LongRingBufferMap stores number of last mappings long to T. It is meant to be 
 * used to storing last timed samples T
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LongRingBufferMap<T>
{
    private final long[] keys;
    private final T[] values;
    private int index;
    private int size;
    private final int capacity;
    private final boolean inOrder;
    /**
     * Creates new LongRingBufferMap expecting keys in-order.
     * @param capacity 
     */
    public LongRingBufferMap(int capacity)
    {
        this(capacity, true);
    }
    /**
     * Creates new LongRingBufferMap expecting keys in-order if inOrder is true.
     * Otherwise keys can be in any order. Get methods will scan all keys for a
     * closest match.
     * @param capacity
     * @param inOrder 
     */
    public LongRingBufferMap(int capacity, boolean inOrder)
    {
        this.capacity = capacity;
        this.inOrder = inOrder;
        this.keys = new long[capacity];
        this.values = (T[]) new Object[capacity];
    }
    /**
     * Put new key/value pair.
     * <p>Note! Key order is not checked even if inOrder is true.
     * @param key
     * @param value 
     */
    public void put(long key, T value)
    {
        int idx = Math.floorMod(index, capacity);
        keys[idx] = key;
        values[idx] = value;
        index++;
        if (size < capacity)
        {
            size++;
        }
    }
    /**
     * Returns first exact match.
     * @param key
     * @return 
     */
    public T get(long key)
    {
        return getClosest(key, 0);
    }
    /**
     * Returns first match which differs delta maximum.
     * @param key
     * @param delta
     * @return 
     */
    public T getClosest(long key, long delta)
    {
        long max = Long.MAX_VALUE;
        int closest = -1;
        int idx = Math.floorMod(index-size, capacity);
        for (int ii=0;ii<size;ii++)
        {
            int i = Math.floorMod(idx+ii, capacity);
            long dif = Math.abs(key - keys[i]);
            if (dif < max)
            {
                closest = i;
                max = dif;
            }
            else
            {
                if (inOrder)
                {
                    break;
                }
            }
        }
        if (closest != -1 && max <= delta)
        {
            return values[closest];
        }
        else
        {
            return null;
        }
    }
    /**
     * Calls c for each key/value pair.
     * @param c 
     */
    public void forEach(ObjLongConsumer<T> c)
    {
        int idx = Math.floorMod(index-size, capacity);
        for (int ii=0;ii<size;ii++)
        {
            int i = Math.floorMod(idx+ii, capacity);
            c.accept(values[i], keys[i]);
        }
    }
    public Set<Long> keys()
    {
        Set<Long> set = new HashSet<>();
        int idx = Math.floorMod(index-size, capacity);
        for (int ii=0;ii<size;ii++)
        {
            int i = Math.floorMod(idx+ii, capacity);
            set.add(keys[i]);
        }
        return set;
    }
    public Collection<T> values()
    {
        List<T> list = new ArrayList<>(size);
        for (T t : values)
        {
            if (t == null)
            {
                break;
            }
            list.add(t);
        }
        return list;
    }
    public int size()
    {
        return size;
    }
    public boolean isEmpty()
    {
        return size == 0;
    }
    public void clear()
    {
        index = 0;
        size = 0;
    }
}
