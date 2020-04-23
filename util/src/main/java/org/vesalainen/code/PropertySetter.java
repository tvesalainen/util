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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import org.vesalainen.util.ArrayHelp;
import org.vesalainen.util.function.BooleanConsumer;

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
    default void set(String property, boolean arg){}
    default void set(String property, byte arg){}
    default void set(String property, char arg){}
    default void set(String property, short arg){}
    default void set(String property, int arg){}
    default void set(String property, long arg){}
    default void set(String property, float arg){}
    default void set(String property, double arg){}
    default <T> void set(String property, T arg){}

    default BooleanConsumer getBooleanConsumer(String property)
    {
        return (v)->set(property, v);
    }

    default <T> Consumer<T> getConsumer(String property)
    {
        return (v)->set(property, v);
    }

    default DoubleConsumer getDoubleConsumer(String property)
    {
        return (v)->set(property, v);
    }

    default IntConsumer getIntConsumer(String property)
    {
        return (v)->set(property, v);
    }

    default LongConsumer getLongConsumer(String property)
    {
        return (v)->set(property, v);
    }
}
