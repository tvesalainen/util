/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.function;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Funcs
{
    public static <T,U> T same(T t, U u)
    {
        return t;
    }
    public static <T,U,V> T same(T t, U u, V v)
    {
        return t;
    }
    public static boolean same(boolean arg)
    {
        return arg;
    }
    public static byte same(byte arg)
    {
        return arg;
    }
    public static char same(char arg)
    {
        return arg;
    }
    public static short same(short arg)
    {
        return arg;
    }
    public static int same(int arg)
    {
        return arg;
    }
    public static long same(long arg)
    {
        return arg;
    }
    public static float same(float arg)
    {
        return arg;
    }
    public static double same(double arg)
    {
        return arg;
    }
    public static Object same(Object arg)
    {
        return arg;
    }
}
