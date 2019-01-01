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
import java.util.stream.LongStream;

/**
 * TimeoutSlidingAverage calculates angle average for given timeout.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeoutSlidingAngleAverage extends AbstractSlidingAngleAverage implements TimeArray
{
    protected final long timeout;
    protected long[] times;
    protected Clock clock;
    /**
     * Creates TimeoutSlidingAngleAverage
     * @param initialSize Initial size of buffers
     * @param timeout Sample timeout
     */
    public TimeoutSlidingAngleAverage(int initialSize, long timeout)
    {
        this(Clock.systemUTC(), initialSize, timeout);
    }
    /**
     * Creates TimeoutSlidingAngleAverage
     * @param clock
     * @param initialSize Initial size of buffers
     * @param timeout Sample timeout
     */
    public TimeoutSlidingAngleAverage(Clock clock, int initialSize, long timeout)
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
        return clock.millis()- times[index] > timeout;
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        sin = (double[]) newArray(sin, size, new double[newSize]);
        cos = (double[]) newArray(cos, size, new double[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    @Override
    protected void assign(int index, double value)
    {
        super.assign(index, value);
        times[index] = clock.millis();
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
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[beginMod()];
    }

    @Override
    public long lastTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(endMod()+size-1) % size];
    }

    @Override
    public long previousTime()
    {
        if (count() < 1)
        {
            throw new IllegalStateException("count() < 1");
        }
        return times[(endMod()+size-2) % size];
    }
    
    public Clock clock()
    {
        return clock;
    }

    public void clock(Clock clock)
    {
        this.clock = clock;
    }
    
}
