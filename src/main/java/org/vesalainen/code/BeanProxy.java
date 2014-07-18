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

/**
 *
 * @author Timo Vesalainen
 */
public class BeanProxy
{
    public static <T extends BeanProxy> T getInstance(Class<T> cls)
    {
        try
        {
            BeanProxyClass annotation = cls.getAnnotation(BeanProxyClass.class);
            if (annotation == null)
            {
                throw new IllegalArgumentException("@"+BeanProxyClass.class.getSimpleName()+" missing in cls");
            }
            Class<?> c = Class.forName(annotation.value());
            return (T) c.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    protected void set(String property, boolean arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, byte arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, char arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, short arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, int arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, long arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, float arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, double arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected void set(String property, Object arg) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected boolean getBoolean(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected byte getByte(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected char getChar(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected short getShort(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected int getInt(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected long getLong(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected float getFloat(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected double getDouble(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
    protected Object getObject(String property) throws Exception
    {
        throw new UnsupportedOperationException("not supported.");
    }
}
