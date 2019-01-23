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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.DoubleConsumer;

/**
 * Calculates standard deviation
 * <p>This class is thread-safe
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StandardDeviation implements DoubleConsumer
{
    private double center;
    private double squareSum;
    private long count;
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    protected ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public StandardDeviation()
    {
    }

    public StandardDeviation(double center)
    {
        this.center = center;
    }

    @Override
    public void accept(double value)
    {
        writeLock.lock();
        try
        {
            squareSum += Math.pow(value-center, 2);
            count++;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    public double getStandardDeviation()
    {
        return squareSum/count;
    }

    @Override
    public String toString()
    {
        return "StandardDeviation{" + getStandardDeviation() + '}';
    }
    
}
