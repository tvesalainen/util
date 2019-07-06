/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface FunctionalSetters
{
    public interface FunctionalSetterFactory
    {
        BooleanSetter createBooleanSetter(String property);
        ByteSetter createByteSetter(String property);
        CharSetter createCharSetter(String property);
        ShortSetter createShortSetter(String property);
        IntSetter createIntSetter(String property);
        LongSetter createLongSetter(String property);
        FloatSetter createFloatSetter(String property);
        DoubleSetter createDoubleSetter(String property);
        <T> ObjectSetter<T> createObjectSetter(String property);
    }
    @FunctionalInterface
    public interface BooleanSetter
    {
        void set(boolean value);
    }
    @FunctionalInterface
    public interface ByteSetter
    {
        void set(byte value);
    }
    @FunctionalInterface
    public interface CharSetter
    {
        void set(char value);
    }
    @FunctionalInterface
    public interface ShortSetter
    {
        void set(short value);
    }
    @FunctionalInterface
    public interface IntSetter
    {
        void set(int value);
    }
    @FunctionalInterface
    public interface LongSetter
    {
        void set(long value);
    }
    @FunctionalInterface
    public interface FloatSetter
    {
        void set(float value);
    }
    @FunctionalInterface
    public interface DoubleSetter
    {
        void set(double value);
    }
    @FunctionalInterface
    public interface ObjectSetter<T>
    {
        void set(T value);
    }
}
