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

import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.ObjLongConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectTimeoutSliding<T> extends ObjectAbstractSliding<T>
{
    
    protected final long timeout;
    protected long[] times;
    protected LongSupplier clock;

    public ObjectTimeoutSliding(int initialSize, long timeout)
    {
        this(System::currentTimeMillis, initialSize, timeout);
    }
    public ObjectTimeoutSliding(LongSupplier clock, int initialSize, long timeout)
    {
        super(initialSize);
        this.clock = clock;
        this.timeout = timeout;
        this.times = new long[size];
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
        ring = (T[]) newArray(ring, size, new Object[newSize]);
        times = (long[]) newArray(times, times.length, new long[newSize]);
        size = newSize;
    }

    public void accept(T value)
    {
        accept(value, clock.getAsLong());
    }

    public void accept(T value, long time)
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
    protected void assign(int index, T value)
    {
        throw new UnsupportedOperationException();
    }

    protected void assign(int index, T value, long time)
    {
        ring[index] = value;
        times[index] = time;
    }

    @Override
    protected void remove(int index)
    {
    }

    public void forEach(ObjLongConsumer<T> act)
    {
        readLock.lock();
        try
        {
            PrimitiveIterator.OfInt mi = modIterator();
            while (mi.hasNext())
            {
                act.accept(ring[mi.nextInt()], times[mi.nextInt()]);
            }
        }
        finally
        {
            readLock.unlock();
        }
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
            int cnt = count();
            return (long[]) copy(times, cnt, new long[cnt]);
        }
        finally
        {
            readLock.unlock();
        }
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
}
