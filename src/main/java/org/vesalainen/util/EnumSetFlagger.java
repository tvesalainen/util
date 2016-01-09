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

package org.vesalainen.util;

import java.util.EnumSet;

/**
 * A bridge between bit flag and EnumSet. EnumSet can be converted to flag and
 * vice versa.
 * @author Timo Vesalainen
 */
public class EnumSetFlagger
{
    /**
     * Returns bit flag constructed from EnumSet. Flags bit position is according
     * to Enum ordinal.
     * 
     * <p>Throws IllegalArgumentException if flag overflows
     * @param <E> Enum type
     * @param eSet 
     * @return 
     */
    public static <E extends Enum<E>> int getFlag(EnumSet<E> eSet)
    {
        int flag = 0;
        for (Enum<E> en : eSet)
        {
            int ordinal = en.ordinal();
            if (ordinal >= Integer.SIZE)
            {
                throw new IllegalArgumentException(eSet+" contains too many enums for int");
            }
            flag |= 1 << ordinal;
        }
        return flag;
    }
    /**
     * Returns bit flag constructed from EnumSet. Flags bit position is according
     * to Enum ordinal.
     * 
     * <p>Throws IllegalArgumentException if flag overflows
     * @param <E> Enum type
     * @param eSet 
     * @return 
     */
    public static <E extends Enum<E>> long getLongFlag(EnumSet<E> eSet)
    {
        long flag = 0;
        for (Enum<E> en : eSet)
        {
            long ordinal = en.ordinal();
            if (ordinal >= Long.SIZE)
            {
                throw new IllegalArgumentException(eSet+" contains too many enums for long");
            }
            flag |= 1 << ordinal;
        }
        return flag;
    }
    /**
     * Returns EnumSet constructed from bit flag. Bit flags bit position is
     * according to Enum ordinal.
     * 
     * <p>Throws IllegalArgumentException if enum contains too many values
     * to fit flag.
     * @param <E>
     * @param elementType
     * @param flag
     * @return 
     */
    public static <E extends Enum<E>> EnumSet<E> getSet(Class<E> elementType, int flag)
    {
        EnumSet<E> set = EnumSet.noneOf(elementType);
        E[] enumConstants = elementType.getEnumConstants();
        if (enumConstants.length > Integer.SIZE)
        {
            throw new IllegalArgumentException(elementType+" contains too many enums for int");
        }
        for (int ii=0;ii<enumConstants.length;ii++)
        {
            if ((flag & (1<<ii)) != 0)
            {
                set.add(enumConstants[ii]);
            }
        }
        return set;
    }
    /**
     * Returns EnumSet constructed from bit flag. Bit flags bit position is
     * according to Enum ordinal.
     * 
     * <p>Throws IllegalArgumentException if enum contains too many values
     * to fit flag.
     * @param <E>
     * @param elementType
     * @param flag
     * @return 
     */
    public static <E extends Enum<E>> EnumSet<E> getSet(Class<E> elementType, long flag)
    {
        EnumSet<E> set = EnumSet.noneOf(elementType);
        E[] enumConstants = elementType.getEnumConstants();
        if (enumConstants.length > Long.SIZE)
        {
            throw new IllegalArgumentException(elementType+" contains too many enums for long");
        }
        for (int ii=0;ii<enumConstants.length;ii++)
        {
            if ((flag & (1<<ii)) != 0)
            {
                set.add(enumConstants[ii]);
            }
        }
        return set;
    }
}
