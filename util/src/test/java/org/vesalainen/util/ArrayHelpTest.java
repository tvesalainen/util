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
package org.vesalainen.util;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayHelpTest
{
    
    public ArrayHelpTest()
    {
    }

    @Test
    public void testConcat()
    {
        double[] array = new double[]{2, 3, 1, 2, 0, 5, 7, 8};
        double[] concat = ArrayHelp.concat(array, 9);
        assertTrue(concat.length == array.length+1);
        assertEquals(9, concat[array.length], 1e-10);
    }
    @Test
    public void testSort1()
    {
        double[] array = new double[]{2, 3, 1, 2, 0, 5, 7, 8};
        double[] exp = new double[]{2, 3, 0, 5, 1, 2, 7, 8};
        ArrayHelp.sort(array, 2, 4, 2);
        assertArrayEquals(exp, array, 1e-10);
    }
    @Test
    public void testArePointsInXOrder()
    {
        double[] array = new double[]{2, 3, 1, 2, 0, 5, 7, 8};
        assertFalse(ArrayHelp.arePointsInXOrder(array));
        ArrayHelp.sort(array, 2);
        assertTrue(ArrayHelp.arePointsInXOrder(array));
    }
    @Test
    public void testFlatten()
    {
        double[][] m = new double[][]{{1,2}, {3,4}, {5,6}};
        double[] exp = new double[]{1,2, 3,4, 5,6};
        assertArrayEquals(exp, ArrayHelp.flatten(m), 1e-10);
        assertTrue(Arrays.deepEquals(m, ArrayHelp.unFlatten(3, exp)));
    }
    @Test
    public void testIndexOf()
    {
        String[] arr = new String[] {"foo", "bar", "qwerty"};
        assertEquals(1, ArrayHelp.indexOf(arr, "bar"));
        assertEquals(-1, ArrayHelp.indexOf(arr, "kiosk"));
    }
    @Test
    public void testContains()
    {
        String[] arr = new String[] {"foo", "bar", "qwerty"};
        assertTrue(ArrayHelp.contains(arr, "bar"));
        assertFalse(ArrayHelp.contains(arr, "kiosk"));
    }
    @Test
    public void testContainsOnly()
    {
        String[] arr = new String[] {"foo", "bar", "qwerty"};
        assertFalse(ArrayHelp.containsOnly(arr, "foo"));
    }
    
}
