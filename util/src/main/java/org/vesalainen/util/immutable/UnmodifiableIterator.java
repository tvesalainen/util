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
package org.vesalainen.util.immutable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A Iterator wrapper where remove method throws UnsupportedOperationException
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnmodifiableIterator<T> implements Iterator<T>
{
    private Iterator<T> iterator;

    public UnmodifiableIterator(Iterator<T> iterator)
    {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    @Override
    public T next()
    {
        return iterator.next();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action)
    {
        iterator.forEachRemaining(action);
    }
    
}
