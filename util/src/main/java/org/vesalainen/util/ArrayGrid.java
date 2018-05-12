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
import java.util.Objects;

/**
 * <p> if grid is boxed x and y values must stay within their width and heigth
 boundaries. If grid is not boxed x outside width boundary will flow to next 
 * or previous line as long as resulting coordinates are in grib.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayGrid<T> implements Grid<T>
{
    protected T[] array;
    protected int width;
    protected int heigth;
    protected int length;
    protected boolean boxed;

    public ArrayGrid(T[] array, int width)
    {
        this(array, width, false);
    }
    public ArrayGrid(T[] array, int width, boolean boxed)
    {
        this(array, width, array.length/width, array.length, boxed);
    }

    public ArrayGrid(int width, int heigth)
    {
        this(width, heigth, false);
    }
    public ArrayGrid(int width, int heigth, boolean boxed)
    {
        this(null, width, heigth, width*heigth, boxed);
    }

    public ArrayGrid(T[] array, int width, int heigth, int length, boolean boxed)
    {
        this.array = array;
        this.width = width;
        this.heigth = heigth;
        this.length = length;
        this.boxed = boxed;
    }

    public T[] getArray()
    {
        return array;
    }

    @Override
    public boolean hit(int x, int y, T color)
    {
        if (!inBox(x, y))
        {
            return false;
        }
        return hit(position(x, y), color);
    }

    public boolean hit(int position, T color)
    {
        if (position < 0 || position >= length || !inBox(position))
        {
            return false;
        }
        T c = getColor(position);
        return Objects.equals(c, color);
    }

    protected int line(int position)
    {
        return position / width;
    }

    protected int column(int position)
    {
        return position % width;
    }

    protected int position(int x, int y)
    {
        return y * width + x;
    }

    @Override
    public void setColor(int x, int y, T color)
    {
        setColor(position(x, y), color);
    }

    @Override
    public T getColor(int x, int y)
    {
        if (!inBox(x, y))
        {
            return null;
        }
        return getColor(position(x, y));
    }


    protected boolean inBox(int position)
    {
        return inBox(column(position), line(position));
    }

    protected boolean inBox(int x, int y)
    {
        return !boxed || (x >= 0 && x < width && y >= 0 && y < heigth);
    }

    @Override
    public int width()
    {
        return width;
    }

    @Override
    public int heigth()
    {
        return heigth;
    }

    private void setColor(int position, T color)
    {
        checkPosition(position);
        if (array == null)
        {
            if (color == null)
            {
                return;
            }
            array = (T[]) Array.newInstance(color.getClass(), length);
        }
        array[position] = color;
    }

    private T getColor(int position)
    {
        checkPosition(position);
        if (array == null)
        {
            return null;
        }
        return array[position];
    }
    private void checkPosition(int position)
    {
        if (position < 0 || position > length)
        {
            throw new IllegalArgumentException("illegal position "+position);
        }
    }
}
