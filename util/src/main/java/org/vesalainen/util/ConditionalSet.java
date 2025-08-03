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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Predicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConditionalSet<T> extends HashSet<T>
{
    private Predicate<T> predicate;

    public ConditionalSet(Predicate<T> predicate)
    {
        this.predicate = predicate;
    }

    public ConditionalSet(Collection<? extends T> c, Predicate<T> predicate)
    {
        super(c);
        this.predicate = predicate;
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return new ConditionalSpliterator(super.spliterator(), predicate);
    }

    @Override
    public Object clone()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T e)
    {
        return super.add(e);
    }

    @Override
    public boolean contains(Object o)
    {
        if (predicate.test((T) o))
        {
            return super.contains(o);
        }
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return new ConditionalIterator(super.iterator(), predicate);
    }

    @Override
    public int size()
    {
        return (int) stream().count();
    }
    
}
