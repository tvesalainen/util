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

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ValueDescriptions
{
    private int id;
    private Supplier<String> name;
    private List<ValueDescription> valDesc;

    public ValueDescriptions(int id, String name, List<ValueDescription> valDesc)
    {
        this(id, ()->name, valDesc);
    }

    public ValueDescriptions(String name, List<ValueDescription> valDesc)
    {
        this(0, ()->name, valDesc);
    }

    public ValueDescriptions(int id, Supplier<String> name, List<ValueDescription> valDesc)
    {
        this.id = id;
        this.name = name;
        this.valDesc = valDesc;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name.get();
    }

    public List<ValueDescription> getValDesc()
    {
        return valDesc;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + this.id;
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
        final ValueDescriptions other = (ValueDescriptions) obj;
        if (this.id != other.id)
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.valDesc, other.valDesc))
        {
            return false;
        }
        return true;
    }
    
}
