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
package org.vesalainen.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class SlidingAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingAverageTest()
    {
    }

    @Test
    public void test1()
    {
        SlidingAverage sa = new SlidingAverage(2);
        sa.add(1);
        assertEquals(1, sa.fast(), Epsilon);
        assertEquals(1, sa.average(), Epsilon);
        sa.add(3);
        assertEquals(2, sa.fast(), Epsilon);
        assertEquals(2, sa.average(), Epsilon);
        sa.add(5);
        assertEquals(4, sa.fast(), Epsilon);
        assertEquals(4, sa.average(), Epsilon);
        sa.add(7);
        assertEquals(6, sa.fast(), Epsilon);
        assertEquals(6, sa.average(), Epsilon);
    }
    
}
