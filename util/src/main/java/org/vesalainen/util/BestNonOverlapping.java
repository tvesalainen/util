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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * BestNonOverlapping helps choose best ranges from a series of overlapping
 ranges. Best in a sense of given comparator.
 * <p>One solution is to find best radio broadcasts so that they don't overlap.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BestNonOverlapping
{
    /**
     * Selects the best range so that no better range is overlapping it.
     * @param stream
     * @return 
     */
    public static final <T,U extends Range<T>> U best(Stream<U> stream, Comparator<U> comp)
    {
        return best(stream.iterator(), comp);
    }
    /**
     * Selects the best range so that no better range is overlapping it.
     * @param iterator
     * @return 
     */
    public static final <T,U extends Range<T>> U best(Iterator<U> iterator, Comparator<U> comp)
    {
        List<U> list = new ArrayList<>();
        ListIterator<U> li = list.listIterator();
        if (!iterator.hasNext())
        {
            throw new IllegalArgumentException("empty");
        }
        U prev = iterator.next();
        li.add(prev);
        while (iterator.hasNext())
        {
            U next = iterator.next();
            if (!prev.isOverlapping(next) || CollectionHelp.compare(prev, next, comp) >= 0)
            {
                break;
            }
            prev = next;
            li.add(prev);
        }
        Range<T> top = li.previous();
        while (li.hasPrevious())
        {
            Range<T> p = li.previous();
            if (top.isOverlapping(p))
            {
                li.remove();
            }
            else
            {
                top = p;
            }
        }
        return list.get(0);
    }
}
