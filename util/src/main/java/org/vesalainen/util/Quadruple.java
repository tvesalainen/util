/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * bu;t WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.util;

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Quadruple<A,B,C,D>
{
    private static final long serialVersionUID = 1L;
    
    protected A first;
    protected B second;
    protected C third;
    protected D fourth;

    public Quadruple(A first, B second, C third, D fourth)
    {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public A getFirst()
    {
        return first;
    }

    public B getSecond()
    {
        return second;
    }

    public C getThird()
    {
        return third;
    }

    public D getFourth()
    {
        return fourth;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.first);
        hash = 37 * hash + Objects.hashCode(this.second);
        hash = 37 * hash + Objects.hashCode(this.third);
        hash = 37 * hash + Objects.hashCode(this.fourth);
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
        final Quadruple<?, ?, ?, ?> other = (Quadruple<?, ?, ?, ?>) obj;
        if (!Objects.equals(this.first, other.first))
        {
            return false;
        }
        if (!Objects.equals(this.second, other.second))
        {
            return false;
        }
        if (!Objects.equals(this.third, other.third))
        {
            return false;
        }
        if (!Objects.equals(this.fourth, other.fourth))
        {
            return false;
        }
        return true;
    }

}
