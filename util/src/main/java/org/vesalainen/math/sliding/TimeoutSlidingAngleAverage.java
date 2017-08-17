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
     * @param size Initial size of buffers
     * @param timeout Sample timeout
     */
    public TimeoutSlidingAngleAverage(int size, long timeout)
    {
        this(Clock.systemUTC(), size, timeout);
    }
    /**
     * Creates TimeoutSlidingAngleAverage
     * @param clock
     * @param size Initial size of buffers
     * @param timeout Sample timeout
     */
    public TimeoutSlidingAngleAverage(Clock clock, int size, long timeout)
    {
        super(size);
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
    
    public Clock clock()
    {
        return clock;
    }

    public void clock(Clock clock)
    {
        this.clock = clock;
    }
    
}
