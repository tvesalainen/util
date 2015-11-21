/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.io;

import java.lang.reflect.Field;
import java.util.UUID;
import org.vesalainen.util.BitArray;

/**
 *
 * @author tkv
 * @param <T>
 */
public abstract class CompressedIO<T> implements AutoCloseable
{
    protected final T obj;
    protected final Class<? extends Object> cls;
    protected byte[] buf1;
    protected byte[] bits;
    protected BitArray bitArray;
    protected Field[] fields;
    protected int bytes;
    protected UUID uuid;

    public CompressedIO(T obj)
    {
        this.obj = obj;
        
        cls = obj.getClass();
    }

    public UUID getUuid()
    {
        return uuid;
    }

    protected int getBytes(String type)
    {
        switch (type)
        {
            case "boolean":
            case "byte":
                return 1;
            case "char":
            case "short":
                return 2;
            case "int":
            case "float":
                return 4;
            case "long":
            case "double":
                return 8;
            default:
                throw new IllegalArgumentException(type + " not allowed");
        }
    }
    
}
