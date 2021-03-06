/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dbc;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiplexerIndicator
{
    private int value = -1;

    public MultiplexerIndicator()
    {
    }

    public MultiplexerIndicator(int value)
    {
        this.value = value;
    }
    
    public boolean isMultiplexor()
    {
        return value == -1;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 61 * hash + this.value;
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
        final MultiplexerIndicator other = (MultiplexerIndicator) obj;
        if (this.value != other.value)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return value == -1 ? "M" : "m"+value;
    }
    
}
