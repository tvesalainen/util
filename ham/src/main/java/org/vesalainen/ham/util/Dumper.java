/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.util;

import java.lang.reflect.Array;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Dumper
{
    public static String array(Object array)
    {
        Class<? extends Object> cls = array.getClass();
        if (!cls.isArray())
        {
            throw new IllegalArgumentException("not an array");
        }
        Class<?> componentType = cls.getComponentType();
        if (!componentType.isPrimitive())
        {
            throw new IllegalArgumentException("not a primitive array");
        }
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(componentType.getSimpleName()).append("[]{");
        for (int ii=0;ii<length;ii++)
        {
            if (ii != 0)
            {
                sb.append(", ");
            }
            sb.append(Array.get(array, ii).toString());
        }
        sb.append("};");
        return sb.toString();
    }
}
