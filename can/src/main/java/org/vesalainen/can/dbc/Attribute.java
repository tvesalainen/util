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
import org.vesalainen.io.AppendablePrinter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Attribute
{
    private ObjectType target;
    private String name;
    private AttributeValueType type;
    private Object def;

    public Attribute(ObjectType target, String name, AttributeValueType type)
    {
        this.target = target;
        this.name = name;
        this.type = type;
    }

    void printDefinition(AppendablePrinter out)
    {
        switch (target)
        {
            case GLOBAL:
                out.format("BA_DEF_ \"%s\" %s;\n", name, type.getType());
                break;
            default:
                out.format("BA_DEF_ %s \"%s\" %s;\n", target, name, type.getType());
                break;
        }
    }
    void printDefault(AppendablePrinter out)
    {
        out.format("BA_DEF_DEF_ \"%s\" %s;\n", name, type.getDefault(def));
    }

    void printValue(AppendablePrinter out, Object value)
    {
        out.format("BA_ \"%s\" %s;\n", name, type.getValue(value));
    }
    public String getValue(Object value)
    {
        return type.getValue(value);
    }
    public String getName()
    {
        return name;
    }

    public AttributeValueType getType()
    {
        return type;
    }

    public ObjectType getTarget()
    {
        return target;
    }

    public Object getDef()
    {
        return def;
    }

    public void setDef(Object def)
    {
        this.def = def;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.target);
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.type);
        hash = 37 * hash + Objects.hashCode(this.def);
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
        final Attribute other = (Attribute) obj;
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (this.target != other.target)
        {
            return false;
        }
        if (!Objects.equals(this.type, other.type))
        {
            return false;
        }
        if (!Objects.equals(this.def, other.def))
        {
            return false;
        }
        return true;
    }

}
