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
package org.vesalainen.io;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.util.BitArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class CompressedIO implements AutoCloseable
{
    protected  String source;
    protected ByteBuffer bb1;
    protected byte[] bits;
    protected BitArray bitArray;
    protected int bytes;
    protected UUID uuid;
    protected Map<String,Property> properties = new HashMap<>();
    protected Map<String,Property> unmodifiableProperties = Collections.unmodifiableMap(properties);
    /**
     * Creates CompressedIO with given source
     * @param source 
     */
    protected CompressedIO(String source)
    {
        this.source = source;
    }
    /**
     * Returns UUID
     * @return 
     */
    public UUID getUuid()
    {
        return uuid;
    }
    /**
     * Returns number of properties
     * @return 
     */
    public int getPropertyCount()
    {
        return unmodifiableProperties.size();
    }
    /**
     * Return unmodifiable view of properties
     * @return 
     */
    public Map<String, Property> getProperties()
    {
        return unmodifiableProperties;
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
    public Object get(String property)
    {
        Property prop = properties.get(property);
        if (prop == null)
        {
            throw new IllegalArgumentException(property+" not found");
        }
        switch (prop.type)
        {
            case "boolean":
                return getBoolean(property);
            case "byte":
                return getByte(property);
            case "char":
                return getChar(property);
            case "short":
                return getShort(property);
            case "int":
                return getInt(property);
            case "float":
                return getFloat(property);
            case "long":
                return getLong(property);
            case "double":
                return getDouble(property);
            default:
                throw new IllegalArgumentException(prop.type + " not allowed");
        }
    }
    public boolean getBoolean(String property)
    {
        Property prop = check(property, "boolean");
        byte b = bb1.get(prop.offset);
        return b != 0;
    }
    public byte getByte(String property)
    {
        Property prop = check(property, "byte");
        return bb1.get(prop.offset);
    }
    public char getChar(String property)
    {
        Property prop = check(property, "char");
        return bb1.getChar(prop.offset);
    }
    public short getShort(String property)
    {
        Property prop = check(property, "short");
        return bb1.getShort(prop.offset);
    }
    public int getInt(String property)
    {
        Property prop = check(property, "int");
        return bb1.getInt(prop.offset);
    }
    public long getLong(String property)
    {
        Property prop = check(property, "long");
        return bb1.getLong(prop.offset);
    }
    public float getFloat(String property)
    {
        Property prop = check(property, "float");
        return bb1.getFloat(prop.offset);
    }
    public double getDouble(String property)
    {
        Property prop = check(property, "double");
        return bb1.getDouble(prop.offset);
    }
    protected Property check(String property, String type)
    {
        Property prop = properties.get(property);
        if (prop == null)
        {
            throw new IllegalArgumentException(property+" not found");
        }
        if (!prop.type.equals(type))
        {
            throw new IllegalArgumentException(property+" is illegal type");
        }
        return prop;
    }
    
    protected class Property implements Comparable<Property>
    {
        private String name;
        private String type;
        private int size;
        private int offset;
        private boolean selfImportant;

        protected Property(String name, String type, int offset)
        {
            this(name, type, offset, true);
        }

        protected Property(String name, String type, int offset, boolean selfImportant)
        {
            this.name = name;
            this.type = type;
            this.size = getBytes(type);
            this.offset = offset;
            this.selfImportant = selfImportant;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }

        public int getSize()
        {
            return size;
        }

        public int getOffset()
        {
            return offset;
        }

        public boolean isSelfImportant()
        {
            return selfImportant;
        }

        @Override
        public int compareTo(Property o)
        {
            return offset - o.offset;
        }

        protected void set(PropertySetter setter)
        {
            switch (type)
            {
                case "boolean":
                    setter.set(name, getBoolean(name));
                    break;
                case "byte":
                    setter.set(name, getByte(name));
                    break;
                case "char":
                    setter.set(name, getChar(name));
                    break;
                case "short":
                    setter.set(name, getShort(name));
                    break;
                case "int":
                    setter.set(name, getInt(name));
                    break;
                case "float":
                    setter.set(name, getFloat(name));
                    break;
                case "long":
                    setter.set(name, getLong(name));
                    break;
                case "double":
                    setter.set(name, getDouble(name));
                    break;
                default:
                    throw new UnsupportedOperationException(type + " not supported");
            }
        }
        
    }
}
