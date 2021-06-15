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

import org.vesalainen.can.dict.AttributeValueType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AttributeDefinition
{
    private ObjectType objectType;
    private String name;
    private AttributeValueType type;

    public AttributeDefinition(ObjectType objectType, String name, AttributeValueType type)
    {
        this.objectType = objectType;
        this.name = name;
        this.type = type;
    }

    public AttributeDefinition(String name, AttributeValueType type)
    {
        this.name = name;
        this.type = type;
    }
    
}
