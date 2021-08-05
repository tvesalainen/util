/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.stream.IntStream;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface IntRange extends Comparable<IntRange>
{

    /**
     * Returns true is n is inside this range
     * @param n
     * @return
     */
    default boolean accept(int n)
    {
        return (getFrom() <= n) && (getTo() > n);
    }

    @Override
    default int compareTo(IntRange o)
    {
        if (getFrom() != o.getFrom())
        {
            return getFrom() - o.getFrom();
        }
        else
        {
            return o.getTo() - getTo();
        }
    }

    /**
     * Returns true if argument is inside this range
     * @param r
     * @return
     */
    default boolean contains(IntRange r)
    {
        if (r != null)
        {
            return (getFrom() <= r.getFrom()) && (getTo() >= r.getTo());
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns true if from - to is inside this range
     * @param from
     * @param to
     * @return
     */
    default boolean contains(int from, int to)
    {
        return (getFrom() <= from) && (getTo() >= to);
    }

    /**
     * Returns true is n is inside this range
     * @param n
     * @return
     */
    default boolean contains(int n)
    {
        return accept(n);
    }

    /**
     * Returns the lowest character value
     * @return
     */
    int getFrom();

    /**
     * Returns the greatest character value +1
     * @return
     */
    int getTo();

    /**
     * Returns true if this and other have common characters
     * @param other
     * @return
     */
    default boolean intersect(IntRange other)
    {
        return other.accept(getFrom()) || other.accept(getTo()-1) || accept(other.getFrom()) || accept(other.getTo()-1);
    }
    /**
     * Returns to - from.
     * @return 
     */
    default int getSize()
    {
        return getTo() - getFrom();
    }
    /**
     * Returns IntStream of all items.
     * @return 
     */
    default IntStream stream()
    {
        return IntStream.range(getFrom(), getTo());
    }
}
