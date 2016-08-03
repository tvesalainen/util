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

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * An Iterator for array
 * @author tkv
 * @param <T>
 */
public class ArrayIterator<T> implements Iterator<T>
{
    private final Object array;
    private int index;
    private int length;

    public ArrayIterator(T[] array)
    {
        this(array, 0, array.length);
    }
    public ArrayIterator(T[] array, int offset, int length)
    {
        this.array = array;
        this.index = offset;
        this.length = array.length;
    }
    public ArrayIterator(Object array)
    {
        if (!array.getClass().isArray())
        {
            throw new IllegalArgumentException(array+" is not array");
        }
        this.array = array;
        this.length = Array.getLength(array);
    }
    
    @Override
    public boolean hasNext()
    {
        return (index < length);
    }

    @Override
    public T next()
    {
        return (T) Array.get(array, index++);
    }
    
}
