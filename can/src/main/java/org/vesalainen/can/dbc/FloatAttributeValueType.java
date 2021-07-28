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
public class FloatAttributeValueType extends AttributeValueType
{

    private final Double min;
    private final Double max;

    public FloatAttributeValueType(Double d1, Double d2)
    {
        this.min = d1;
        this.max = d2;
    }

    @Override
    String getType()
    {
        return String.format("FLOAT %s %s", min, max);
    }

    @Override
    Object convType(Object value)
    {
        if (value instanceof Number)
        {
            Number n = (Number) value;
            return n.doubleValue();
        }
        throw new IllegalArgumentException(value+" is not float");
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.min);
        hash = 61 * hash + Objects.hashCode(this.max);
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
        final FloatAttributeValueType other = (FloatAttributeValueType) obj;
        if (!Objects.equals(this.min, other.min))
        {
            return false;
        }
        if (!Objects.equals(this.max, other.max))
        {
            return false;
        }
        return true;
    }
    
}
