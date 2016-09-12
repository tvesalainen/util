/*
 * Copyright (C) 2016 tkv
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
 * Converts set methods to fire except Object and boolean valued which throws
 * IllegalArgmentException as default
 * @author tkv
 */
public interface FloatFire extends PropertySetter
{
    void fire(String property, float value);

    @Override
    default void set(String property, Object arg)
    {
        throw new IllegalArgumentException("set("+property+", "+arg+") called");
    }

    @Override
    default void set(String property, double arg)
    {
        fire(property, (float) arg);
    }

    @Override
    default void set(String property, float arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, long arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, int arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, short arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, char arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, byte arg)
    {
        fire(property, arg);
    }

    @Override
    default void set(String property, boolean arg)
    {
        throw new IllegalArgumentException("set("+property+", "+arg+") called");
    }
    
}
