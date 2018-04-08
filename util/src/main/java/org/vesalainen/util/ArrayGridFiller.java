/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayGridFiller<T> extends GridFiller<T>
{
    private T[] array;
    
    public ArrayGridFiller(int width, int heigth, Strategy strategy, Class<T> cls)
    {
        this(width, heigth, true, strategy, cls);
    }
    public ArrayGridFiller(int width, int heigth, boolean boxed, Strategy strategy, Class<T> cls)
    {
        this(width, heigth, boxed, strategy, (T[])Array.newInstance(cls, width*heigth));
    }
    public ArrayGridFiller(int width, int heigth, Strategy strategy, T[] array)
    {
        this(width, heigth, true, strategy, array);
    }
    public ArrayGridFiller(int width, int heigth, boolean boxed, Strategy strategy, T[] array)
    {
        super(width, heigth, boxed, strategy);
        this.array = array;
        if (array.length < length)
        {
            throw new IllegalArgumentException("array too short");
        }
    }

    public T[] getArray()
    {
        return array;
    }

    @Override
    public T getColor(int position)
    {
        if (position < 0 || position >= length)
        {
            throw new IllegalArgumentException(position+" invalid");
        }
        return array[position];
    }

    @Override
    public void setColor(int position, T color)
    {
        if (position < 0 || position >= length)
        {
            throw new IllegalArgumentException(position+" invalid");
        }
        array[position] = color;
    }
    
    
}
