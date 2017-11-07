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
import java.util.function.Predicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConditionalIterator<T> implements Iterator<T>
{
    private Iterator<T> iterator;
    private Predicate<T> predicate;
    private T next;

    public ConditionalIterator(Iterator<T> iterator, Predicate<T> predicate)
    {
        this.iterator = iterator;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext()
    {
        if (next != null)
        {
            return true;
        }
        while (iterator.hasNext())
        {
            next = iterator.next();
            if (predicate.test(next))
            {
                return true;
            }
            else
            {
                next = null;
            }
        }
        return false;
    }

    @Override
    public T next()
    {
        if (next != null)
        {
            T n = next;
            next = null;
            return n;
        }
        else
        {
            throw new NoSuchElementException();
        }
    }
    
}
