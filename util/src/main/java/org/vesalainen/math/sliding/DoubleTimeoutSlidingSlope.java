/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import static java.lang.Math.*;
import java.util.PrimitiveIterator;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingSlope extends DoubleAbstractTimeoutSliding
{
    private double sxi;
    private double syi;

    protected DoubleTimeoutSlidingSlope(int initialSize, long timeout)
    {
        this(System::currentTimeMillis, initialSize, timeout, (l)->l);
    }
    protected DoubleTimeoutSlidingSlope(LongSupplier clock, int initialSize, long timeout, LongToDoubleFunction timeConv)
    {
        super(clock, initialSize, timeout, timeConv);
    }
    public double slope()
    {
        readLock.lock();
        try
        {
            int count = count();
            double xa = sxi/count;
            double ya = syi/count;
            double s1 = 0;
            double s2 = 0;
            PrimitiveIterator.OfInt it = modIterator();
            while (it.hasNext())
            {
                int ii = it.nextInt();
                double xi = timeConv.applyAsDouble(times[ii]);
                double yi = ring[ii];
                s1 += (xi - xa)*(yi - ya);
                s2 += sq(xi - xa);
            }
            return s1/s2;
        }
        finally
        {
            readLock.unlock();
        }
    }
    @Override
    protected void addSample(double td, double value)
    {
        sxi += td;
        syi += value;
    }

    @Override
    protected void removeSample(double xi, double yi)
    {
        sxi -= xi;
        syi -= yi;
    }
    
    private static double sq(double x)
    {
        return x*x;
    }

    @Override
    public String toString()
    {
        return "Slope="+slope();
    }

}
