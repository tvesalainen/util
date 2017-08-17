/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.Serializable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <F> First type
 * @param <S> Second type
 */
public class Pair<F,S> implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private F first;
    private S second;

    public Pair(F first, S second)
    {
        this.first = first;
        this.second = second;
    }

    public F getFirst()
    {
        return first;
    }

    public void setFirst(F first)
    {
        this.first = first;
    }

    public S getSecond()
    {
        return second;
    }

    public void setSecond(S second)
    {
        this.second = second;
    }
    
}
