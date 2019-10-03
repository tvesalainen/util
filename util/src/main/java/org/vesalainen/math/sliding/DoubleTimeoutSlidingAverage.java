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

import java.time.Clock;
import java.util.Arrays;
import java.util.function.LongSupplier;
import java.util.stream.LongStream;

/**
 * DoubleTimeoutSlidingAverage calculates average for given timeout.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingAverage extends DoubleAbstractSlidingAverage implements Timeouting, TimeArray
{
    protected final long timeout;
    protected long[] times;
    protected LongSupplier clock;
    /**
     * Creates TimeoutSlidingAverage
     * @param size Initial size of ring buffer
     * @param timeout Sample timeout
     */
    public DoubleTimeoutSlidingAverage(int size, long timeout)
    {
        this(System::currentTimeMillis, size, timeout);
    }
    public DoubleTimeoutSlidingAverage(Clock clock, int initialSize, long timeout)
    {
        this(clock::millis, initialSize, timeout);
    }
    /**
     * Creates TimeoutSlidingAverage
     * @param clock
     * @param initialSize Initial size of ring buffer
     * @param timeout Sample timeout
     */
    public DoubleTimeoutSlidingAverage(LongSupplier clock, int initialSize, long timeout)
    {
        super(initialSize);
        this.clock = clock;
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
        return clock.getAsLong()- times[index] > timeout;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    @Override
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
        super.assign(index, value);
        times[index] = time;
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
            return times[(endMod()+size-1) % size];
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
            return times[(endMod()+size-2) % size];
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
    
}
