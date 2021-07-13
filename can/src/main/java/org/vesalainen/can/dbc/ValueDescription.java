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

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ValueDescription
{
    private int value;
    private String description;

    public ValueDescription(int value, String description)
    {
        this.value = value;
        this.description = description;
    }

    public int getValue()
    {
        return value;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + this.value;
        hash = 53 * hash + Objects.hashCode(this.description);
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
        final ValueDescription other = (ValueDescription) obj;
        if (this.value != other.value)
        {
            return false;
        }
        if (!Objects.equals(this.description, other.description))
        {
            return false;
        }
        return true;
    }
    
}
