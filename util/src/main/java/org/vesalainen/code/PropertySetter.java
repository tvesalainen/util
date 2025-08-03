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

import org.vesalainen.code.setter.BooleanSetter;
import org.vesalainen.code.setter.ByteSetter;
import org.vesalainen.code.setter.CharSetter;
import org.vesalainen.code.setter.DoubleSetter;
import org.vesalainen.code.setter.FloatSetter;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.code.setter.ObjectSetter;
import org.vesalainen.code.setter.Setter;
import org.vesalainen.code.setter.ShortSetter;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen
 */
public interface PropertySetter
{
    /**
     * @deprecated Use getProperties(). Prefix handling is not supported anymore.
     * Returns list of interested property prefixes
     * @return 
     */
    default String[] getPrefixes()
    {
        throw new UnsupportedOperationException("getPrefixes() deprecated use getProperties()");
    }
    /**
     * Returns list of interested properties
     * @return 
     */
    default String[] getProperties()
    {
        return getPrefixes();
    }
    /**
     * Returns true if property is one that getProperties() returns.
     * @param property
     * @return 
     */
    default boolean wantsProperty(String property)
    {
        return ArrayHelp.contains(getProperties(), property);
    }
    default void setProperty(String property, Object arg)
    {
        throw new UnsupportedOperationException("Not supported for property '"+property+"'");
    }

    default void set(String property, boolean arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, byte arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, char arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, short arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, int arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, long arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, float arg)
    {
        setProperty(property, arg);
    }

    default void set(String property, double arg)
    {
        setProperty(property, arg);
    }

    default <T> void set(String property, T arg)
    {
        setProperty(property, arg);
    }


    default Setter getSetter(String property, Class<?> type)
    {
        switch (type.getSimpleName())
        {
            case "boolean":
                return getBooleanSetter(property);
            case "byte":
                return getByteSetter(property);
            case "char":
                return getCharSetter(property);
            case "short":
                return getShortSetter(property);
            case "int":
                return getIntSetter(property);
            case "long":
                return getLongSetter(property);
            case "float":
                return getFloatSetter(property);
            case "double":
                return getDoubleSetter(property);
            default:
                return getObjectSetter(property);
        }
    }
    default BooleanSetter getBooleanSetter(String property)
    {
        return (v)->set(property, v);
    }

    default ByteSetter getByteSetter(String property)
    {
        return (v)->set(property, v);
    }

    default CharSetter getCharSetter(String property)
    {
        return (v)->set(property, v);
    }

    default ShortSetter getShortSetter(String property)
    {
        return (v)->set(property, v);
    }

    default IntSetter getIntSetter(String property)
    {
        return (v)->set(property, v);
    }

    default LongSetter getLongSetter(String property)
    {
        return (v)->set(property, v);
    }

    default FloatSetter getFloatSetter(String property)
    {
        return (v)->set(property, v);
    }

    default DoubleSetter getDoubleSetter(String property)
    {
        return (v)->set(property, v);
    }

    default <T> ObjectSetter<T> getObjectSetter(String property)
    {
        return (v)->set(property, v);
    }

}
