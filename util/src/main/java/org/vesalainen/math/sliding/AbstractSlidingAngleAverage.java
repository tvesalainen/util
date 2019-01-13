/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.PrimitiveIterator;

/**
 * Base class for sliding angle average calculation
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSlidingAngleAverage extends AbstractSlidingAverage
{
    
    protected double[] cos;
    protected double[] sin;
    protected double cosSum;
    protected double sinSum;

    protected AbstractSlidingAngleAverage(int initialSize)
    {
        super(initialSize);
        this.cos = new double[size];
        this.sin = new double[size];
    }
    /**
     * Add new value in radians
     * @param rad 
     */
    public void acceptRad(double rad)
    {
        accept(Math.toDegrees(rad));
    }
    /**
     * Adds new angle
     * @param value In Degrees
     */
    @Override
    public void accept(double value)
    {
        if (value < 0 || value > 360)
        {
            throw new IllegalArgumentException(value + " not degree");
        }
        super.accept(value);
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
    /**
     * Returns fast average. Fast calculation adds and subtracts values from
     * sum field. This might cause difference in time to actual calculating
     * sample by sample.
     * @return 
     */
    @Override
    public double fast()
    {
        readLock.lock();
        try
        {
            int count = count();
            return toDegrees(sinSum / count, cosSum / count);
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Returns sample by sample calculated average.
     * @return 
     */
    @Override
    public double average()
    {
        readLock.lock();
        try
        {
            int count = count();
            double s = 0;
            double c = 0;
            PrimitiveIterator.OfInt it = modIterator();
            while (it.hasNext())
            {
                int m = it.nextInt();
                s += sin[m];
                c += cos[m];
            };
            return toDegrees(s / count, c / count);
        }
        finally
        {
            readLock.unlock();
        }
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
