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
package org.vesalainen.util;

import java.util.Objects;

/**
 * Range implements from to range between.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class Range<T extends Comparable<T>>
{
    protected T from;
    protected T to;
    /**
     * Creates new Range
     * @param other 
     */
    public Range(Range<T> other)
    {
        this.from = other.from;
        this.to = other.to;
    }
    
    /**
     * Creates new Range
     * @param from
     * @param to 
     */
    public Range(T from, T to)
    {
        this.from = from;
        this.to = to;
    }
    /**
     * Returns true if ranges overlap. In another words either range has others
     * from or to in-range or ranges are equal.
     * @param other
     * @return 
     */
    public boolean isOverlapping(Range<T> other)
    {
        return  equals(other) ||
                isInRange(other.from, false, false) ||
                isInRange(other.to, false, false) ||
                other.isInRange(from, false, false) ||
                other.isInRange(to, false, false);
    }
    /**
     * Returns true if item is greater or equals to from and less than to.
     * @param item
     * @return 
     */
    public boolean isInRange(T item)
    {
        return isInRange(item, true, false);
    }
    /**
     * Returns true if item is in range.
     * @param item
     * @param fromIncluded
     * @param toIncluded
     * @return 
     */
    public boolean isInRange(T item, boolean fromIncluded, boolean toIncluded)
    {
        if (fromIncluded && from.compareTo(item) == 0)
        {
            return true;
        }
        if (toIncluded && to.compareTo(item) == 0)
        {
            return true;
        }
        if (from.compareTo(to) <= 0)
        {
            if (from.compareTo(item) < 0)
            {
                return to.compareTo(item) > 0;
            }
            return false;
        }
        else
        {
            return from.compareTo(item) < 0 || to.compareTo(item) > 0;
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
        final Range<?> other = (Range<?>) obj;
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
