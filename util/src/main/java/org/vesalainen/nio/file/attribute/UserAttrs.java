/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.vesalainen.lang.Primitives;

/**
 * Helper class for user-defined-attributes
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class UserAttrs
{
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setShortAttribute(Path path, String attribute, short value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, Primitives.writeShort(value), options);
    }
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setIntAttribute(Path path, String attribute, int value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, Primitives.writeInt(value), options);
    }
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setLongAttribute(Path path, String attribute, long value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, Primitives.writeLong(value), options);
    }
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setFloatAttribute(Path path, String attribute, float value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, Primitives.writeFloat(value), options);
    }
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setDoubleAttribute(Path path, String attribute, double value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, Primitives.writeDouble(value), options);
    }
    /**
     * Set user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param value
     * @param options
     * @throws IOException 
     */
    public static final void setStringAttribute(Path path, String attribute, String value, LinkOption... options) throws IOException
    {
        Files.setAttribute(path, attribute, value.getBytes(UTF_8), options);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final short getShortAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        if (attr.length != 2)
        {
            throw new IllegalArgumentException(attribute+" not correct type");
        }
        return Primitives.readShort(attr);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final int getIntAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        if (attr.length != 4)
        {
            throw new IllegalArgumentException(attribute+" not correct type");
        }
        return Primitives.readInt(attr);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final long getLongAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        if (attr.length != 8)
        {
            throw new IllegalArgumentException(attribute+" not correct type");
        }
        return Primitives.readLong(attr);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final float getFloatAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        if (attr.length != 4)
        {
            throw new IllegalArgumentException(attribute+" not correct type");
        }
        return Primitives.readFloat(attr);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final double getDoubleAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        if (attr.length != 8)
        {
            throw new IllegalArgumentException(attribute+" not correct type");
        }
        return Primitives.readDouble(attr);
    }
    /**
     * Returns user-defined-attribute
     * @param path
     * @param attribute user:attribute name
     * @param options
     * @return
     * @throws IOException 
     */
    public static final String getStringAttribute(Path path, String attribute, LinkOption... options) throws IOException
    {
        byte[] attr = (byte[]) Files.getAttribute(path, attribute, options);
        if (attr == null)
        {
            throw new IllegalArgumentException(attribute+" not found");
        }      
        return new String(attr, UTF_8);
    }
}
