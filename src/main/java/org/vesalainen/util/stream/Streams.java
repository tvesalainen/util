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
package org.vesalainen.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.vesalainen.util.function.IntBiPredicate;

/**
 * Stream helpers
 * @author tkv
 */
public class Streams
{
    /**
     * Compares two streams using natural order
     * @param <T>
     * @param s1
     * @param s2
     * @return 
     */
    public static <T> int compare(IntStream s1, IntStream s2)
    {
        return compare(s1, s2, null);
    }
    /**
     * Compares two streams using given comparator
     * @param <T>
     * @param s1
     * @param s2
     * @param comp
     * @return 
     */
    public static <T> int compare(IntStream s1, IntStream s2, IntBinaryOperator comp)
    {
        PrimitiveIterator.OfInt os1 = s1.iterator();
        PrimitiveIterator.OfInt os2 = s2.iterator();
        while (os1.hasNext() && os2.hasNext())
        {
            int n1 = os1.next();
            int n2 = os2.next();
            int c;
            if (comp == null)
            {
                c = n1 - n2;
            }
            else
            {
                c = comp.applyAsInt(n1, n2);
            }
            if (c != 0)
            {
                return c;
            }
        }
        boolean hn1 = os1.hasNext();
        boolean hn2 = os2.hasNext();
        if (hn1 == hn2)
        {
            return 0;
        }
        if (hn1)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
    /**
     * Compares two streams using natural order
     * @param <T>
     * @param s1
     * @param s2
     * @return 
     */
    public static <T> int compare(Stream<T> s1, Stream<T> s2)
    {
        return compare(s1, s2, null);
    }
    /**
     * Compares two streams using given comparator
     * @param <T>
     * @param s1
     * @param s2
     * @param comp
     * @return 
     */
    public static <T> int compare(Stream<T> s1, Stream<T> s2, Comparator<T> comp)
    {
        Iterator<T> os1 = s1.iterator();
        Iterator<T> os2 = s2.iterator();
        Comparable<T> comparable = null;
        while (os1.hasNext() && os2.hasNext())
        {
            T n1 = os1.next();
            T n2 = os2.next();
            int c;
            if (comp == null)
            {
                comparable = (Comparable<T>) n1;
                c = comparable.compareTo(n2);
            }
            else
            {
                c = comp.compare(n1, n2);
            }
            if (c != 0)
            {
                return c;
            }
        }
        boolean hn1 = os1.hasNext();
        boolean hn2 = os2.hasNext();
        if (hn1 == hn2)
        {
            return 0;
        }
        if (hn1)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
    /**
     * Tests if two streams are equal using equals.
     * @param <T>
     * @param s1
     * @param s2
     * @return 
     * @see java.util.Objects#equals(java.lang.Object, java.lang.Object) 
     */
    public static <T> boolean equals(Stream<T> s1, Stream<T> s2)
    {
        return equals(s1, s2, Objects::equals);
    }
    /**
     * Tests if two streams are equal using given equals.
     * @param <T>
     * @param s1
     * @param s2
     * @param eq
     * @return 
     */
    public static <T> boolean equals(Stream<T> s1, Stream<T> s2, BiPredicate<T,T> eq)
    {
        Iterator<T> os1 = s1.iterator();
        Iterator<T> os2 = s2.iterator();
        while (os1.hasNext() && os2.hasNext())
        {
            if (!eq.test(os1.next(), os2.next()))
            {
                return false;
            }
        }
        return os1.hasNext() == os2.hasNext();
    }
}
