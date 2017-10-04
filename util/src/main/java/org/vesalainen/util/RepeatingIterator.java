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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * RepeatingIterator keeps iterating over collection while collection is not empty.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RepeatingIterator<T> implements Iterator<T>
{
    private Iterable<T> iterable;
    private Iterator<T> iterator;

    public RepeatingIterator(Iterable<T> iterable)
    {
        this.iterable = iterable;
        this.iterator = iterable.iterator();
        if (!iterator.hasNext())
        {
            iterator = null;
        }
    }
    
    @Override
    public boolean hasNext()
    {
        if (iterator == null)
        {
            return false;
        }
        if (!iterator.hasNext())
        {
            iterator = iterable.iterator();
            if (!iterator.hasNext())
            {
                iterator = null;
                return false;
            }
        }
        return true;
    }

    @Override
    public T next()
    {
        if (iterator == null)
        {
            throw new NoSuchElementException();
        }
        return iterator.next();
    }
    
}
