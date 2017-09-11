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
package org.vesalainen.lang;

/**
 * Casts class provides unsigned conversions from lower precision number to 
 * higher as well as conversions from higher to lower throwing 
 * IllegalArgumentException if conversion causes change.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Casts
{
    public static final int castUnsignedInt(byte v)
    {
        return v & 0xff;
    }
    public static final int castUnsignedInt(short v)
    {
        return v & 0xffff;
    }
    public static final long castUnsignedLong(byte v)
    {
        return v & 0xff;
    }
    public static final long castUnsignedLong(short v)
    {
        return v & 0xffff;
    }
    public static final long castUnsignedLong(int v)
    {
        return v & 0xffffffffL;
    }
    public static final long castLong(double v)
    {
        if (v > Long.MAX_VALUE || v < Long.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to long");
        }
        return (long) v;
    }
    public static final int castUnsignedInt(long v)
    {
        if ((v & 0xffffffff) != v)
        {
            throw new IllegalArgumentException(v+" cannot be cast to int");
        }
        return (int) v;
    }
    public static final int castInt(long v)
    {
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to int");
        }
        return (int) v;
    }
    public static final int castInt(double v)
    {
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to int");
        }
        return (int) v;
    }
    public static final short castUnsignedShort(long v)
    {
        if ((v & 0xffff) != v)
        {
            throw new IllegalArgumentException(v+" cannot be cast to unsigned short");
        }
        return (short) v;
    }
    public static final short castShort(long v)
    {
        if (v > Short.MAX_VALUE || v < Short.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to short");
        }
        return (short) v;
    }
    public static final short castShort(double v)
    {
        if (v > Short.MAX_VALUE || v < Short.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to short");
        }
        return (short) v;
    }
    public static final byte castByte(long v)
    {
        if (v > Byte.MAX_VALUE || v < Byte.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to byte");
        }
        return (byte) v;
    }
    public static final byte castByte(double v)
    {
        if (v > Byte.MAX_VALUE || v < Byte.MIN_VALUE)
        {
            throw new IllegalArgumentException(v+" cannot be cast to byte");
        }
        return (byte) v;
    }
}
