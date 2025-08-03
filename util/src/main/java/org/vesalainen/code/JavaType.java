/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.code;

import java.util.EnumSet;
import javax.lang.model.type.TypeKind;

/**
 *
 * @author Timo Vesalainen
 */
public enum JavaType
{
    BOOLEAN("boolean"),
    BYTE("byte"),
    CHAR("char"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    DECLARED("Object");
    
    private final String code;
    
    private JavaType(String name)
    {
        this.code = name;
    }

    public String getCode()
    {
        return code;
    }
    
}
