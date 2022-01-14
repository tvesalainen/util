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

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DoubleAbstractTimeoutSliding extends DoubleAbstractSliding implements TimeArray, Timeouting
{
    
    protected final long timeout;
    protected long[] times;
    protected LongSupplier clock;
    protected LongToDoubleFunction timeConv;

    protected DoubleAbstractTimeoutSliding(LongSupplier clock, int initialSize, long timeout, LongToDoubleFunction timeConv)
    {
        super(initialSize);
        this.clock = clock;
        this.timeConv = timeConv;
        this.timeout = timeout;
        this.times = new long[size];
    }

    @Override
    public long maxDuration()
    {
        return timeout;
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return clock.getAsLong() - times[index] > timeout;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    public void accept(double value)
    {
        accept(value, clock.getAsLong());
    }

    public void accept(double value, long time)
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
            assign(endMod(), value, time);
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
        throw new UnsupportedOperationException();
    }

    protected void assign(int index, double value, long time)
    {
        ring[index] = value;
        times[index] = time;
        double td = timeConv.applyAsDouble(time);
        addSample(td, value);
    }

    @Override
    protected void remove(int index)
    {
        double xi = timeConv.applyAsDouble(times[index]);
        double yi = ring[index];
        removeSample(xi, yi);
    }

    /**
     * Returns time values as array. Returned array is independent.
     * @return
     */
    public long[] toTimeArray()
    {
        readLock.lock();
        try
        {
            return (long[]) copy(times, size, new long[count()]);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Returns a stream of sample times
     * @return
     */
    @Override
    public LongStream timeStream()
    {
        return Arrays.stream(toTimeArray());
    }

    /**
     * Returns a stream time1, value1, time2, value2, ...
     * @return 
     */
    public DoubleStream pointStream()
    {
        long[] xArray;
        double[] yArray;
        readLock.lock();
        try
        {
            xArray = toTimeArray();
            yArray = toArray();
        }
        finally
        {
            readLock.unlock();
        }
        if (xArray.length != yArray.length)
        {
            throw new IllegalArgumentException();
        }
        int length = xArray.length;
        double[] array = new double[length*2];
        for (int ii=0;ii<length;ii++)
        {
            array[2*ii] = xArray[ii];
            array[2*ii+1] = yArray[ii];
        }
        return Arrays.stream(array);
    }

    @Override
    public long[] getTimes()
    {
        return times;
    }

    @Override
    public int getSize()
    {
        return size;
    }

    @Override
    public long getTimeout()
    {
        return timeout;
    }

    public long getTime(int index)
    {
        if (count() <= 0)
        {
            throw new IllegalStateException("count() < 1");
        }
        if (index < 0 || index >= count())
        {
            throw new IllegalStateException("index out of bounds");
        }
        return times[(beginMod()+index) % size];
    }
    @Override
    public long firstTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[beginMod()];
    }

    @Override
    public long lastTime()
    {
        readLock.lock();
        try
        {
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
            }
            return times[(endMod() + size - 1) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public long previousTime()
    {
        readLock.lock();
        try
        {
            if (count() < 2)
            {
                throw new IllegalStateException("count() < 2");
            }
            return times[(endMod() + size - 2) % size];
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public LongSupplier clock()
    {
        return clock;
    }

    @Override
    public void clock(LongSupplier clock)
    {
        this.clock = clock;
    }

    public void forEach(TimeValueConsumer act)
    {
        PrimitiveIterator.OfInt mi = modIterator();
        while (mi.hasNext())
        {
            int ii = mi.nextInt();
            act.accept(times[ii], ring[ii]);
        }
    }

    protected abstract void addSample(double td, double value);

    protected abstract void removeSample(double xi, double yi);
    
}
