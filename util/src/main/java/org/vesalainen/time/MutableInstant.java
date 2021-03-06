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
package org.vesalainen.time;

import java.time.Instant;
import static java.time.temporal.ChronoField.*;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MutableInstant implements Comparable<MutableInstant>
{
    private static final long T3 = 1000L;
    private static final long T6 = 1000000L;
    private static final long T9 = 1000000000L;
    protected long second;
    protected long nano;
    private ReentrantReadWriteLock rcLock = new ReentrantReadWriteLock();
    protected ReadLock rl = rcLock.readLock();
    protected WriteLock wl = rcLock.writeLock();

    public MutableInstant()
    {
    }

    public MutableInstant(TemporalAccessor accessor)
    {
        this(accessor.getLong(INSTANT_SECONDS), accessor.getLong(NANO_OF_SECOND));
    }

    public MutableInstant(MutableInstant mi)
    {
        this(mi.second, mi.nano);
    }

    public MutableInstant(long second, long nano)
    {
        set(second, nano);
    }
    public static MutableInstant now()
    {
        return new MutableInstant(Instant.now());
    }
    public long until(MutableInstant mi)
    {
        rl.lock();
        try
        {
            long ds = mi.second-second;
            long dn = mi.nano-nano;
            return Math.multiplyExact(ds, T9)+dn;
        }
        finally
        {
            rl.unlock();
        }
    }
    public void set(MutableInstant mi)
    {
        set(mi.second, mi.nano);
    }
    public void plus(long nano)
    {
        set(this.second, Math.addExact(this.nano, nano));
    }
    public void setNanoTime()
    {
        set(System.nanoTime());
    }
    public void set(long nano)
    {
        set(0, nano);
    }
    public final void set(long second, long nano)
    {
        wl.lock();
        try
        {
            this.second = second + Math.floorDiv(nano, T9);
            this.nano = Math.floorMod(nano, T9);
        }
        finally
        {
            wl.unlock();
        }
    }
    public long millis()
    {
        rl.lock();
        try
        {
            return Math.multiplyExact(T3, second)+nano/T6;
        }
        finally
        {
            rl.unlock();
        }
    }
    public Instant instant()
    {
        rl.lock();
        try
        {
            return Instant.ofEpochSecond(second, nano);
        }
        finally
        {
            rl.unlock();
        }
    }
    public boolean isEqual(Instant instant)
    {
        return second == instant.getEpochSecond() && nano == instant.getNano();
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + (int) (this.second ^ (this.second >>> 32));
        hash = 97 * hash + (int) (this.nano ^ (this.nano >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MutableInstant other = (MutableInstant) obj;
        if (this.second != other.second)
        {
            return false;
        }
        if (this.nano != other.nano)
        {
            return false;
        }
        return true;
    }
    
    long second()
    {
        return second;
    }

    long nano()
    {
        return nano;
    }

    @Override
    public String toString()
    {
        return "MutableInstant{" + instant() +'}';
    }

    @Override
    public int compareTo(MutableInstant o)
    {
        if (second != o.second)
        {
            return Primitives.signum(second - o.second);
        }
        else
        {
            return Primitives.signum(nano - o.nano);
        }
    }
    
}
