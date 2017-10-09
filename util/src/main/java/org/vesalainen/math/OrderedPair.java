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
package org.vesalainen.math;

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OrderedPair<T,V>
{
    private T firstEntry;
    private V secondEntry;

    public OrderedPair(T firstEntry, V secondEntry)
    {
        this.firstEntry = firstEntry;
        this.secondEntry = secondEntry;
    }

    public T getFirstEntry()
    {
        return firstEntry;
    }

    public V getSecondEntry()
    {
        return secondEntry;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.firstEntry);
        hash = 47 * hash + Objects.hashCode(this.secondEntry);
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
        final OrderedPair<?, ?> other = (OrderedPair<?, ?>) obj;
        if (!Objects.equals(this.firstEntry, other.firstEntry))
        {
            return false;
        }
        if (!Objects.equals(this.secondEntry, other.secondEntry))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "(" + firstEntry + ", " + secondEntry + ')';
    }
    
}
