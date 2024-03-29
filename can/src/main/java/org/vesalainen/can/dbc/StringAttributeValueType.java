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
public class StringAttributeValueType extends AttributeValueType
{
    public static final StringAttributeValueType STRING_ATTRIBUTE_VALUE_TYPE = new StringAttributeValueType();
    
    private StringAttributeValueType()
    {
    }

    @Override
    String getType()
    {
        return "STRING";
    }

    @Override
    protected String getValue(Object value)
    {
        return String.format("\"%s\"", value);
    }

    @Override
    protected String getDefault(Object def)
    {
        return String.format("\"%s\"", def);
    }

    @Override
    Object convType(Object value)
    {
        return (String)value;
    }
    
}
