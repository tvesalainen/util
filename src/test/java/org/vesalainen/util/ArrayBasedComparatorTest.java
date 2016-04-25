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
package org.vesalainen.util;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class ArrayBasedComparatorTest
{
    
    public ArrayBasedComparatorTest()
    {
    }

    @Test
    public void test1()
    {
        ArrayBasedComparator<Integer> abc = new ArrayBasedComparator<>(3, 1, 2);
        Integer[] us = new Integer[] { 1, 2, 2, 3, 4, 1};
        Integer[] exp = new Integer[] { 3, 1, 1, 2, 2, 4};
        Arrays.sort(us, abc);
        assertArrayEquals(exp, us);
    }
    
}
