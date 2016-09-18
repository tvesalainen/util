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

import java.time.Clock;
import java.util.Arrays;
import java.util.stream.LongStream;

/**
 * Base class for timeout sliding bound calculation. Each sample has given timeout.
 * @author tkv
 */
public abstract class AbstractTimeoutSlidingBound extends AbstractSlidingBound implements Timeouting, TimeArray
{
    protected final long timeout;
    protected long[] times;
    Timeouting parent;
    protected Clock clock;
    /**
     * 
     * @param size Initial ringbuffer size
     * @param timeout Sample timeout in millis.
     */
    protected AbstractTimeoutSlidingBound(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    /**
     * 
     * @param clock
     * @param size Initial ringbuffer size
     * @param timeout Sample timeout in millis.
     */
    protected AbstractTimeoutSlidingBound(Clock clock, int size, long timeout)
    {
        super(size);
        this.clock = clock;
        this.timeout = timeout;
        this.times = new long[size];
    }
    /**
     * 
     * @param parent
     */
    AbstractTimeoutSlidingBound(Timeouting parent)
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
        eliminate();
        if (parent == null && end-begin >= size-margin)
        {
            grow();
        }
        assign(end % size, value);
        int e = end;
        while (e > begin && exceedsBounds(e, value))
        {
            e--;
            assign(e % size, value);
        }
        end++;
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
        ring[index] = value;
        if (parent == null)
        {
            times[index] = clock().millis();
        }
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return clock().millis() - times[index] > timeout;
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
    public double count()
    {
        return end-begin;
    }

    /**
     * Returns a stream of sample times
     * @return 
     */
    @Override
    public LongStream timeStream()
    {
        if (begin == end)
        {
            return LongStream.empty();
        }
        else
        {
            int b = begin % size;
            int e = end % size;
            if (b < e)
            {
                return Arrays.stream(times, b, e);
            }
            else
            {
                return LongStream.concat(Arrays.stream(times, e, size), Arrays.stream(times, 0, b));
            }
        }
    }

    @Override
    public long firstTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[begin % size];
    }

    @Override
    public long lastTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(end+size-1) % size];
    }

    @Override
    public long previousTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(end+size-2) % size];
    }

    @Override
    public Clock clock()
    {
        if (parent != null)
        {
            return parent.clock();
        }
        return clock;
    }

    @Override
    public void clock(Clock clock)
    {
        this.clock = clock;
    }
    
}
