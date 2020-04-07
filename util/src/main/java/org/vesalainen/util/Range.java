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

import java.util.Comparator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public interface Range<T> extends Comparable<Range<T>>
{
    /**
     * Returns comparator or null using natural order.
     * @return 
     */
    default Comparator<T> comparator()
    {
        return null;
    }
    default int compare(T o1, T o2)
    {
        return CollectionHelp.compare(o1, o2, comparator());
    }
    /**
     * Uses from as key
     * @param o
     * @return
     */
    @Override
    default int compareTo(Range<T> o)
    {
        return compare(getFrom(), o.getFrom());
    }

    T getFrom();

    T getTo();

    /**
     * Returns true if item is greater or equals to from and less than to.
     * @param item
     * @return
     */
    default boolean isInRange(T item)
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
    default boolean isInRange(T item, boolean fromIncluded, boolean toIncluded)
    {
        if (fromIncluded && compare(getFrom(), item) == 0)
        {
            return true;
        }
        if (toIncluded && compare(getTo(), item) == 0)
        {
            return true;
        }
        if (compare(getFrom(), getTo()) <= 0)
        {
            if (compare(getFrom(), item) < 0)
            {
                return compare(getTo(), item) > 0;
            }
            return false;
        }
        else
        {
            return compare(getFrom(), item) < 0 || compare(getTo(), item) > 0;
        }
    }

    /**
     * Returns true if ranges overlap. In another words either range has others
     * from or to in-range or ranges are equal.
     * @param other
     * @return
     */
    default boolean isOverlapping(Range<T> other)
    {
        return  equals(other) ||
                isInRange(other.getFrom(), false, false) ||
                isInRange(other.getTo(), false, false) ||
                other.isInRange(getFrom(), false, false) ||
                other.isInRange(getTo(), false, false);
    }
    /**
     * Returns true if to is greater or equal than from
     * @return 
     */
    default boolean isInOrder()
    {
        return compare(getFrom(), getTo()) <= 0;
    }
}
