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
package org.vesalainen.math.sliding;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class SlidingBoundsTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingBoundsTest()
    {
    }

    @Test
    public void testSlidingMax()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        SlidingMax sm = new SlidingMax(3);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(6);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(11);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(9, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testSlidingMin()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        SlidingMin sm = new SlidingMin(3);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
        sm.add(6);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(10);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(11);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testTimedSlidingMin()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        TimedSlidingMax sm = new TimedSlidingMax(3, 100);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
        sm.add(6);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(10);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(11);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
    }
    
}
