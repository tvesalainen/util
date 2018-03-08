/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * TemporalAccessorRange implements from to range between TemporalAccessor's.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class TemporalAccessorRange<T extends TemporalAccessor & Comparable<T>>
{
    protected T from;
    protected T to;
    /**
     * Creates new TemporalAccessorRange
     * @param from
     * @param to 
     */
    public TemporalAccessorRange(T from, T to)
    {
        this.from = from;
        this.to = to;
    }
    /**
     * Returns true if instant is between from and to endpoints included
     * @param instant
     * @return 
     */
    public boolean isInRange(T instant)
    {
        if (from.compareTo(to) <= 0)
        {
            if (from.compareTo(instant) <= 0)
            {
                return to.compareTo(instant) >= 0;
            }
            return false;
        }
        else
        {
            return from.compareTo(instant) <= 0 || to.compareTo(instant) >= 0;
        }
    }

    public T getFrom()
    {
        return from;
    }

    public T getTo()
    {
        return to;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.from);
        hash = 19 * hash + Objects.hashCode(this.to);
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
        final TemporalAccessorRange<?> other = (TemporalAccessorRange<?>) obj;
        if (!Objects.equals(this.from, other.from))
        {
            return false;
        }
        if (!Objects.equals(this.to, other.to))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TimeRange{" + "from=" + from + ", to=" + to + '}';
    }
}
