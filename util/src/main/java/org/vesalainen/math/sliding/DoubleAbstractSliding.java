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

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

/**
 * Abstract base class for sliding expression calculations. Sliding calculations means 
 * calculating expression for number of last samples or samples that are not older than given time.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DoubleAbstractSliding extends AbstractSliding
{
    protected double[] ring;
    /**
     * 
     * @param initialSize Initial size of ring buffer
     */
    protected DoubleAbstractSliding(int initialSize)
    {
        this.initialSize = initialSize;
        if (Integer.bitCount(initialSize) == 1)
        {
            this.size = initialSize;
        }
        else
        {
            this.size = 2*Integer.highestOneBit(initialSize);
        }
        ring = new double[size];
    }

    /**
     * Assign value to inner storage
     * @param index Mod size
     * @param value 
     */
    protected abstract void assign(int index, double value);
    
    /**
     * Returns values as array. Returned array is independent.
     * @return 
     */
    public double[] toArray()
    {
        readLock.lock();
        try
        {
            int cnt = count();
            return (double[]) copy(ring, cnt, new double[cnt]);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public double getValue(int index)
    {
        if (count() <= 0)
        {
            throw new IllegalStateException("count() < 1");
        }
        if (index < 0 || index >= count())
        {
            throw new IllegalStateException("index out of bounds");
        }
        return ring[(beginMod()+index) % size];
    }

    
    /**
     * Returns values as stream in the same order as entered. Stream is valid 
     * only the time that takes to fill margin number of slots.
     * @return 
     */
    public DoubleStream stream()
    {
        return Arrays.stream(toArray());
    }

    public void forEach(DoubleConsumer act)
    {
        PrimitiveIterator.OfInt mi = modIterator();
        while (mi.hasNext())
        {
            act.accept(ring[mi.nextInt()]);
        }
    }
}
