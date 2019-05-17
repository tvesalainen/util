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

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoundedPriorityQueue<T> extends AbstractQueue<T>
{
    private T[] values;
    private int size;
    private Comparator<T> comparator;

    public BoundedPriorityQueue(int capacity)
    {
        this(capacity, null);
    }
    public BoundedPriorityQueue(int capacity, Comparator<T> comparator)
    {
        this.values = (T[]) new Object[capacity];
        this.comparator = comparator;
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return new ArrayIterator<>(values, 0, size);
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean offer(T e)
    {
        int idx = Arrays.binarySearch(values, 0, size, e, comparator);
        int insertPoint = insertPoint(idx);
        if (size < values.length)
        {
            System.arraycopy(values, insertPoint, values, insertPoint+1, size - insertPoint);
            size++;
        }
        else
        {
            System.arraycopy(values, insertPoint, values, insertPoint+1, size - insertPoint - 1);
        }
        values[insertPoint] = e;
        return true;
    }

    private int insertPoint(int idx)
    {
        return -idx-1;
    }
    @Override
    public T poll()
    {
        if (size > 0)
        {
            T value = values[0];
            System.arraycopy(values, 1, values, 0, --size);
            return value;
        }
        else
        {
            return null;
        }
    }

    @Override
    public T peek()
    {
        if (size > 0)
        {
            return values[0];
        }
        else
        {
            return null;
        }
    }
    
}
