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

import org.vesalainen.io.AppendablePrinter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Attribute
{
    private String name;
    private AttributeValueType type;
    private Object def;
    private Object value;

    public Attribute(String name, AttributeValueType type)
    {
        this.name = name;
        this.type = type;
    }
    
    void printDefinition(AppendablePrinter out)
    {
        out.format("BA_DEF_ \"%s\" %s;\n", name, type.getType());
    }
    void printDefault(AppendablePrinter out)
    {
        out.format("BA_DEF_DEF_ \"%s\" %s;\n", name, type.getDefault(def));
    }

    void printValue(AppendablePrinter out)
    {
        out.format("BA_ \"%s\" %s;\n", name, type.getValue(value));
    }
    
    public void setDefault(Object value)
    {
        this.def = value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public AttributeValueType getType()
    {
        return type;
    }

    public Object getDef()
    {
        return def;
    }

    public Object getValue()
    {
        if (value != null)
        {
            return value;
        }
        return def;
    }

}
