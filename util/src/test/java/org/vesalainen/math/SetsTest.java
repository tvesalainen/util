/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SetsTest
{
    private Set<Integer> A = set(1, 2, 3);
    private Set<Integer> B = set(2, 3, 4);
    public SetsTest()
    {
    }

    @Test
    public void testUnion()
    {
        Set<Integer> exp = set(1, 2, 3, 4);
        assertEquals(exp, Sets.union(A, B));
    }
    @Test
    public void testIntersection()
    {
        Set<Integer> exp = set(2, 3);
        assertEquals(exp, Sets.intersection(A, B));
    }
    @Test
    public void testDifference()
    {
        Set<Integer> exp = set(1);
        assertEquals(exp, Sets.difference(A, B));
    }
    @Test
    public void testSymmetricDifference()
    {
        Set<Integer> exp = set(1, 4);
        assertEquals(exp, Sets.symmetricDifference(A, B));
    }
    @Test
    public void testAssign()
    {
        Set<Integer> exp = set(3, 4, 5, 6, 7);
        Set<Integer> set = set(1, 2, 3);
        Sets.assign(exp, set);
        assertEquals(exp, set);
    }
    private static <T> Set<T> set(T... value)
    {
        Set<T> set = new HashSet<>();
        for (T v : value)
        {
            set.add(v);
        }
        return set;
    }
}
