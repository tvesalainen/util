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
package org.vesalainen.math;

import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PointList
{
    private double[] array;
    private int size;

    public PointList()
    {
        this(16);
    }

    public PointList(int initialSize)
    {
        this.array = new double[initialSize];
    }

    public void add(Point2D.Double p)
    {
        add(p.x, p.y);
    }
    public void add(int index, Point2D.Double p)
    {
        add(index, p.x, p.y);
    }
    public void add(double x, double y)
    {
        if (size >= array.length/2)
        {
            grow();
        }
        array[2*size] = x;
        array[2*size+1] = y;
        size++;
    }
    public void add(int index, double x, double y)
    {
        if (index < 0 || index > size)
        {
            throw new IndexOutOfBoundsException();
        }
        if (size >= array.length/2)
        {
            grow();
        }
        System.arraycopy(array, 2*index, array, 2*(index+1), 2*(size-index));
        array[2*index] = x;
        array[2*index+1] = y;
        size++;
    }
    public void remove(int index)
    {
        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(array, 2*(index+1), array, 2*index, 2*(size-index-1));
        size--;
    }
    public Point2D get(int index)
    {
        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        return new Point2D.Double(array[2*index], array[2*index+1]);
    }
    /**
     * Returns copy of array with size*2 length
     * @return 
     */
    public double[] array()
    {
        return Arrays.copyOf(array, 2*size);
    }

    public int size()
    {
        return size;
    }

    public void clear()
    {
        size = 0;
    }
    protected void grow()
    {
        array = Arrays.copyOf(array, array.length*2);
    }
}
