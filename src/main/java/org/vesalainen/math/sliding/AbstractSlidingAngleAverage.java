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
package org.vesalainen.math.sliding;

/**
 *
 * @author tkv
 */
public abstract class AbstractSlidingAngleAverage extends AbstractSlidingAverage
{
    
    protected double[] cos;
    protected double[] sin;
    protected double cosSum;
    protected double sinSum;

    protected AbstractSlidingAngleAverage(int size)
    {
        super(size);
        this.cos = new double[size];
        this.sin = new double[size];
    }

    /**
     * Adds new angle
     * @param value In Degrees
     */
    @Override
    public void add(double value)
    {
        if (value < 0 || value > 360)
        {
            throw new IllegalArgumentException(value + " not degree");
        }
        super.add(value);
    }

    @Override
    protected void assign(int index, double value)
    {
        double rad = Math.toRadians(value);
        double s = Math.sin(rad);
        sin[index] = s;
        double c = Math.cos(rad);
        cos[index] = c;
        sinSum += s;
        cosSum += c;
    }

    @Override
    protected void remove(int index)
    {
        sinSum -= sin[index];
        cosSum -= cos[index];
    }

    @Override
    public double fast()
    {
        int count = end - begin;
        return toDegrees(sinSum / count, cosSum / count);
    }

    @Override
    public double average()
    {
        int count = end - begin;
        double s = 0;
        double c = 0;
        for (int ii = begin; ii < end; ii++)
        {
            s += sin[ii % size];
            c += cos[ii % size];
        }
        return toDegrees(s / count, c / count);
    }

    private double toDegrees(double y, double x)
    {
        double deg = Math.toDegrees(Math.atan2(y, x));
        if (deg < 0)
        {
            return 360.0+deg;
        }
        else
        {
            return deg;
        }
    }
}
