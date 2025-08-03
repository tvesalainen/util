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
package org.vesalainen.math;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 * A container for (x,y) points
 * <p>This class is not thread safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class XYSamples
{
    private double[] xarr;
    private double[] yarr;
    private double minX = Double.POSITIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private int count;
    private IntUnaryOperator growFunc;
    /**
     * Creates XYSamples with initial size 64 and growth function which doubles
     * the size.
     */
    public XYSamples()
    {
        this(64, (s)->s*2);
    }
    /**
     * Creates XYSamples with given initial size and growth function
     * @param initialSize
     * @param growFunc Function parameter is current size, returns new size.
     */
    public XYSamples(int initialSize, IntUnaryOperator growFunc)
    {
        this.growFunc = growFunc;
        xarr = new double[initialSize];
        yarr = new double[initialSize];
    }
    /**
     * Adds all samples  from given XYSamples to this
     * @param samples 
     */
    public void add(XYSamples samples)
    {
        samples.forEach(this::add);
    }
    /**
     * Adds sample and grows if necessary.
     * @param x
     * @param y 
     */
    public void add(double x, double y)
    {
        if (count >= xarr.length)
        {
            grow();
        }
        xarr[count] = x;
        yarr[count++] = y;
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }
    /**
     * Returns sample count.
     * @return 
     */
    public int getCount()
    {
        return count;
    }
    /**
     * Returns x-sample.
     * @param index
     * @return 
     */
    public double getX(int index)
    {
        if (index <= 0 || index >= count)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        return xarr[index];
    }
    /**
     * Returns y-sample.
     * @param index
     * @return 
     */
    public double getY(int index)
    {
        if (index <= 0 || index >= count)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        return yarr[index];
    }
    /**
     * Returns stream of x-values.
     * @return 
     */
    public DoubleStream xStream()
    {
        return Arrays.stream(xarr, 0, count);
    }
    /**
     * Returns stream of y-values.
     * @return 
     */
    public DoubleStream yStream()
    {
        return Arrays.stream(yarr, 0, count);
    }
    /**
     * Calls action for each sample (x,y)
     * @param action 
     */
    public void forEach(DoubleBiConsumer action)
    {
        for (int ii=0;ii<count;ii++)
        {
            action.accept(xarr[ii], yarr[ii]);
        }
    }

    public double getMinX()
    {
        return minX;
    }

    public double getMinY()
    {
        return minY;
    }

    public double getMaxX()
    {
        return maxX;
    }

    public double getMaxY()
    {
        return maxY;
    }
    
    private void grow()
    {
        int newSize = growFunc.applyAsInt(xarr.length);
        xarr = Arrays.copyOf(xarr, newSize);
        yarr = Arrays.copyOf(yarr, newSize);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        forEach((x,y)->sb.append("(").append(x).append(",").append(y).append(")"));
        sb.append("]");
        return sb.toString();
    }
    
}
