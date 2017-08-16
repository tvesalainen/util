/*
 * Copyright (C) 2017 tkv
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

/**
 *
 * @author tkv
 */
public final class ArrayHelp
{
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(byte[] array, byte item)
    {
        for (byte b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(char[] array, char item)
    {
        for (char b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(short[] array, short item)
    {
        for (short b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(int[] array, int item)
    {
        for (int b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(long[] array, long item)
    {
        for (long b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(float[] array, float item)
    {
        for (float b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(double[] array, double item)
    {
        for (double b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members equals item
     * @param <T>
     * @param array
     * @param item
     * @return 
     */
    public static final <T> boolean contains(T[] array, T item)
    {
        for (T b : array)
        {
            if (b.equals(item))
            {
                return true;
            }
        }
        return false;
    }
}
