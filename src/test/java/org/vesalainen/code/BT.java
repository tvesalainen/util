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
@BeanProxyClass(value = "org.vesalainen.code.impl.BTImpl")
public abstract class BT extends BeanProxy implements Intf
{
    private boolean z;
    private byte b;
    private char c;
    private short s;
    private int i;
    private long j;
    private float f;
    private double d;
    private Object o;

    @Override
    protected Object getObject(String property)
    {
        return o;
    }

    @Override
    protected double getDouble(String property)
    {
        return d;
    }

    @Override
    protected float getFloat(String property)
    {
        return f;
    }

    @Override
    protected long getLong(String property)
    {
        return j;
    }

    @Override
    protected int getInt(String property)
    {
        return i;
    }

    @Override
    protected short getShort(String property)
    {
        return s;
    }

    @Override
    protected char getChar(String property)
    {
        return c;
    }

    @Override
    protected byte getByte(String property)
    {
        return b;
    }

    @Override
    protected boolean getBoolean(String property)
    {
        return z;
    }

    @Override
    protected void set(String property, Object arg)
    {
        o = arg;
    }

    @Override
    protected void set(String property, double arg)
    {
        d = arg;
    }

    @Override
    protected void set(String property, float arg)
    {
        f = arg;
    }

    @Override
    protected void set(String property, long arg)
    {
        j = arg;
    }

    @Override
    protected void set(String property, int arg)
    {
        i = arg;
    }

    @Override
    protected void set(String property, short arg)
    {
        s = arg;
    }

    @Override
    protected void set(String property, char arg)
    {
        c = arg;
    }

    @Override
    protected void set(String property, byte arg)
    {
        b = arg;
    }

    @Override
    protected void set(String property, boolean arg)
    {
        z = arg;
    }
    
}
