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
import java.util.function.LongSupplier;
import java.util.stream.LongStream;

/**
 * Base class for timeout sliding bound calculation. Each sample has given timeout.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DoubleAbstractTimeoutSlidingBound extends DoubleAbstractSlidingBound implements Timeouting, TimeArray
{
    private final long timeout;
    private long[] times;
    Timeouting parent;
    private LongSupplier clock;
    /**
     * Creates DoubleAbstractTimeoutSlidingBound
     * @param initialSize Initial ringbuffer size
     * @param timeout Sample timeout in millis.
     */
    protected DoubleAbstractTimeoutSlidingBound(int initialSize, long timeout)
    {
        this(System::currentTimeMillis, initialSize, timeout);
    }
    /**
     * Creates DoubleAbstractTimeoutSlidingBound. Clock and timeout have to be in 
     * same units. (currentTimeMillis in millis and nanoTime in nanos)
     * @param clock
     * @param initialSize Initial ringbuffer size
     * @param timeout Sample timeout in millis.
     */
    protected DoubleAbstractTimeoutSlidingBound(LongSupplier clock, int initialSize, long timeout)
    {
        super(initialSize);
        this.clock = clock;
        this.timeout = timeout;
        this.times = new long[size];
    }
    /**
     * 
     * @param parent
     */
    DoubleAbstractTimeoutSlidingBound(Timeouting parent)
    {
        super(parent.getSize());
        this.clock = parent.clock();
        this.parent = parent;
        this.timeout = parent.getTimeout();
        this.times = parent.getTimes();
    }

    @Override
    public long maxDuration()
    {
        return timeout;
    }
    
    /**
     * Add new value
     * @param value 
     */
    @Override
    public void accept(double value)
    {
        accept(value, clock().getAsLong());
    }
    public void accept(double value, long time)
    {
        writeLock.lock();
        try
        {
            eliminate();
            if (parent == null && count() >= size)
            {
                grow();
            }
            endIncr();
            PrimitiveIterator.OfInt rev = modReverseIterator();
            int e = rev.nextInt();
            assign(e, value, time);
            while (rev.hasNext())
            {
                e = rev.nextInt();
                if (exceedsBounds(e, value))
                {
                    assign(e, value, time);
                }
                else
                {
                    break;
                }
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        ring = (double[]) newArray(ring, size, new double[newSize]);
        if (parent == null)
        {
            times = (long[]) newArray(times, times.length, new long[newSize]);
        }
        else
        {
            times = parent.getTimes();
        }
        size = newSize;
    }

    @Override
    protected void assign(int index, double value)
    {
        throw new UnsupportedOperationException();
    }
    protected void assign(int index, double value, long time)
    {
        ring[index] = value;
        if (parent == null)
        {
            times[index] = time;
        }
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return clock().getAsLong() - times[index] > timeout;
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
    public long firstTime()
    {
        readLock.lock();
        try
        {
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
            }
            return times[beginMod()];
        }
        finally
        {
            readLock.unlock();
        }
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
            if (count() < 1)
            {
                throw new IllegalStateException("count() < 1");
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
        if (parent != null)
        {
            return parent.clock();
        }
        return clock;
    }

    @Override
    public void clock(LongSupplier clock)
    {
        this.clock = clock;
    }
    
}
