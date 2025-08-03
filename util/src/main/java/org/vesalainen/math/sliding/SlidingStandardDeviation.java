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
package org.vesalainen.math.sliding;

import java.util.PrimitiveIterator;

/**
 * SlidingStandardDeviation calculates standard deviation for maximum windows-
 * size last values.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingStandardDeviation extends DoubleSlidingAverage
{
    private double stdevSum;
    private double[] stdevRing;
    /**
     * Creates SlidingStandardDeviation with given window-size
     * @param windowSize 
     */
    public SlidingStandardDeviation(int windowSize)
    {
        super(windowSize);
        stdevRing = new double[size];
    }

    @Override
    public void accept(double value)
    {
        writeLock.lock();
        try
        {
            super.accept(value);
            double pow = Math.pow(fast()-value, 2);
            stdevSum += pow;
            stdevRing[Math.floorMod(endMod()-1, size)] = pow;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    @Override
    protected void remove(int index)
    {
        super.remove(index);
        stdevSum -= stdevRing[index];
    }

    /**
     * Returns estimated value of standard deviation. This is faster than
     * standardDeviation()
     * @return 
     */
    public double stdevEstimation()
    {
        return Math.sqrt(stdevSum/count());
    }
    /**
     * 
     * @return Returns standard deviation.
     */
    public double standardDeviation()
    {
        double average = average();
        double powSum = 0;
        PrimitiveIterator.OfInt it = modIterator();
        while (it.hasNext())
        {
            powSum+=Math.pow(ring[it.nextInt()]-average, 2);
        }
        return Math.sqrt(powSum/count());
    }

    @Override
    public String toString()
    {
        return "SlidingStandardDeviation{" + stdevEstimation() + '}';
    }
}
