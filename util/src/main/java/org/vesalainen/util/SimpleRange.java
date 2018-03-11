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
 * SimpleRange implements from to range between.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class SimpleRange<T> implements Range<T>
{
    protected T from;
    protected T to;
    /**
     * Creates new Range
     * @param other 
     */
    public SimpleRange(SimpleRange<T> other)
    {
        this.from = other.from;
        this.to = other.to;
    }
    
    /**
     * Creates new Range
     * @param from
     * @param to 
     */
    public SimpleRange(T from, T to)
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public T getFrom()
    {
        return from;
    }

    @Override
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
        final SimpleRange<?> other = (SimpleRange<?>) obj;
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
        return "Range{" + "from=" + from + ", to=" + to + '}';
    }
}
