/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BitArray
{
    private byte[] array;

    public BitArray(int bits)
    {
        this(new byte[(int)Math.ceil((double)bits/8.0)]);
    }
    public BitArray(byte[] array)
    {
        this.array = array;
    }

    public void set(int index, boolean value)
    {
        if (value)
        {
            array[index / 8] |= (1 << (index % 8));
        }
        else
        {
            array[index / 8] &= ~(1 << (index % 8));
        }
    }

    public boolean isSet(int index)
    {
        return (array[index / 8] & (1 << (index % 8))) != 0;
    }

    public void setAll(boolean value)
    {
        if (value)
        {
            Arrays.fill(array, (byte) 0b11111111);
        }
        else
        {
            Arrays.fill(array, (byte) 0);
        }
    }

    public byte[] getArray()
    {
        return array;
    }
    
}
