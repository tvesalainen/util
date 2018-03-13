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
public interface PropertySetter
{
    /**
     * Returns list of interested property prefixes
     * @return 
     */
    String[] getPrefixes();
    default void set(String property, boolean arg){}
    default void set(String property, byte arg){}
    default void set(String property, char arg){}
    default void set(String property, short arg){}
    default void set(String property, int arg){}
    default void set(String property, long arg){}
    default void set(String property, float arg){}
    default void set(String property, double arg){}
    default void set(String property, Object arg){}
}
