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
import java.util.function.DoubleConsumer;

/**
 * Base class for sliding average calculation
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DoubleAbstractSlidingAverage extends DoubleAbstractSliding implements DoubleConsumer, DoubleValueArray
{
    protected int windowSize;
    protected double sum;
    /**
     * Creates an AbstractSlidingAverage
     * @param windowSize Size of the sliding window
     */
    protected DoubleAbstractSlidingAverage(int windowSize)
    {
        super(windowSize);
        this.windowSize = windowSize;
    }
    /**
     * Adds new value to sliding average
     * @param value 
     */
    @Override
    public void accept(double value)
    {
        writeLock.lock();
        try
        {
            eliminate();
            int count = count();
            if (count >= size)
            {
                grow();
            }
            assign(endMod(), value);
            endIncr();
        }
        finally
        {
            writeLock.unlock();
        }
    }
            
    @Override
    protected void assign(int index, double value)
    {
        ring[index] = value;
        sum += value;
    }

    @Override
    protected void remove(int index)
    {
        sum -= ring[index];
    }

    /**
     * Returns average without actually calculating cell by cell
     * @return 
     */
    public double fast()
    {
        readLock.lock();
        try
        {
            return sum/(double)(count());
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Returns average by calculating cell by cell
     * @return 
     */
    public double average()
    {
        readLock.lock();
        try
        {
            double s = 0;
            PrimitiveIterator.OfInt it = modIterator();
            while (it.hasNext())
            {
                s += ring[it.nextInt()];
            }
            return s/(count());
        }
        finally
        {
            readLock.unlock();
        }
    }
    public double first()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return ring[beginMod()];
    }

    @Override
    public double last()
    {
        readLock.lock();
        try
        {
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
            }
            return ring[(endMod()+size-1) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public double previous()
    {
        readLock.lock();
        try
        {
            if (count() < 2)
            {
                throw new IllegalStateException("count() < 2");
            }
            return ring[(endMod()+size-2) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        return "Average{" +fast()+ '}';
    }
    
}
