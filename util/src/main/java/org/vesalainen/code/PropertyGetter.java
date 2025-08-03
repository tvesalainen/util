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

import org.vesalainen.code.getter.BooleanGetter;
import org.vesalainen.code.getter.ByteGetter;
import org.vesalainen.code.getter.CharGetter;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.FloatGetter;
import org.vesalainen.code.getter.Getter;
import org.vesalainen.code.getter.IntGetter;
import org.vesalainen.code.getter.LongGetter;
import org.vesalainen.code.getter.ObjectGetter;
import org.vesalainen.code.getter.ShortGetter;
import org.vesalainen.code.setter.Setter;

/**
 *
 * @author Timo Vesalainen
 */
public interface PropertyGetter
{
    boolean getBoolean(String property);
    byte getByte(String property);
    char getChar(String property);
    short getShort(String property);
    int getInt(String property);
    long getLong(String property);
    float getFloat(String property);
    double getDouble(String property);
    <T> T getObject(String property);

    default Getter getGetter(String property, Class<?> type)
    {
        switch (type.getSimpleName())
        {
            case "boolean":
                return getBooleanGetter(property);
            case "byte":
                return getByteGetter(property);
            case "char":
                return getCharGetter(property);
            case "short":
                return getShortGetter(property);
            case "int":
                return getIntGetter(property);
            case "long":
                return getLongGetter(property);
            case "float":
                return getFloatGetter(property);
            case "double":
                return getDoubleGetter(property);
            default:
                return getObjectGetter(property);
        }
    }
    default BooleanGetter getBooleanGetter(String property)
    {
        return ()->getBoolean(property);
    }

    default ByteGetter getByteGetter(String property)
    {
        return ()->getByte(property);
    }

    default CharGetter getCharGetter(String property)
    {
        return ()->getChar(property);
    }

    default ShortGetter getShortGetter(String property)
    {
        return ()->getShort(property);
    }

    default IntGetter getIntGetter(String property)
    {
        return ()->getInt(property);
    }

    default LongGetter getLongGetter(String property)
    {
        return ()->getLong(property);
    }

    default FloatGetter getFloatGetter(String property)
    {
        return ()->getFloat(property);
    }

    default DoubleGetter getDoubleGetter(String property)
    {
        return ()->getDouble(property);
    }

    default <T> ObjectGetter<T> getObjectGetter(String property)
    {
        return ()->getObject(property);
    }
}
