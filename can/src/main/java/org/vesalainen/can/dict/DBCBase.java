/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dict;

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.can.dbc.StringAttributeValueType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCBase
{
    
    protected String comment;
    protected Map<String, Attribute> attributes = new HashMap<>();

    public DBCBase()
    {
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setAttribute(String name, String value)
    {
        Attribute attribute = new Attribute(name, new StringAttributeValueType());
        attribute.setValue(value);
        setAttribute(attribute);
    }
    public void setAttribute(Attribute attribute)
    {
        attributes.put(attribute.getName(), attribute);
    }
    
    public String getStringAttribute(String name)
    {
        Attribute attribute = attributes.get(name);
        if (attribute != null && (attribute.getType() instanceof StringAttributeValueType))
        {
            return (String) attribute.getValue();
        }
        return null;
    }
}
