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
import java.io.Serializable;
import java.util.Arrays;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PointList implements Serializable
{
    private static final long serialVersionUID = 1L;
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

    public void add(Point2D p)
    {
        add(p.getX(), p.getY());
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
    public int indexOf(Point2D p)
    {
        return indexOf(p.getX(), p.getY());
    }
    /**
     * Returns first index of point equals given x and y or either given x or y is NaN.
     * @param x
     * @param y
     * @return 
     */
    public int indexOf(double x, double y)
    {
        return indexOf(0, x, y, 0, 0);
    }
    /**
     * Returns first index of point which x differs max deltaX and y differs max
     * deltaY or either given x or y is NaN.
     * @param from
     * @param x
     * @param y
     * @param deltaX
     * @param deltaY
     * @return 
     */
    public int indexOf(int from, double x, double y, double deltaX, double deltaY)
    {
        int len = size();
        for (int ii=from;ii<len;ii++)
        {
            if (
                    (Double.isNaN(x) || Math.abs(x-array[2*ii])<= deltaX) &&
                    (Double.isNaN(y) || Math.abs(y-array[2*ii+1])<= deltaY)
                    )
            {
                return ii;
            }
        }
        return -1;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 89 * hash + Arrays.hashCode(this.array);
        hash = 89 * hash + this.size;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final PointList other = (PointList) obj;
        if (this.size != other.size)
        {
            return false;
        }
        if (!Arrays.equals(this.array, other.array))
        {
            return false;
        }
        return true;
    }
    
    public void forEach(DoubleBiConsumer act)
    {
        int len = size();
        for (int ii=0;ii<len;ii++)
        {
            act.accept(array[2*ii], array[2*ii+1]);
        }
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

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        forEach((x,y)->sb.append('{').append(x).append(", ").append(y).append(')'));
        sb.append('}');
        return sb.toString();
    }
    
}
