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

package org.vesalainen.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class CirclesTest
{
    private static final double Epsilon = 1e-10;
    
    public CirclesTest()
    {
    }

    /**
     * Test of isInside method, of class Circles.
     */
    @Test
    public void testIsInside_3args()
    {
        Circle circle = new AbstractCircle(0, 0, 1);
        assertTrue(Circles.isInside(circle, 0.0, 0.9));
        assertFalse(Circles.isInside(circle, 1.1, 1.1));
    }

    /**
     * Test of isInside method, of class Circles.
     */
    @Test
    public void testIsInside_Circle_Circle()
    {
        Circle c1 = new AbstractCircle(0, 0, 1);
        Circle c2 = new AbstractCircle(0.0, 0.5, 0.4);
        Circle c3 = new AbstractCircle(0.5, 0.5, 1);
        assertTrue(Circles.isInside(c1, c2));
        assertFalse(Circles.isInside(c1, c3));
    }

    /**
     * Test of distanceFromCenter method, of class Circles.
     */
    @Test
    public void testDistanceFromCenter()
    {
        Circle c1 = new AbstractCircle(0, 0, 1);
        assertEquals(1, Circles.distanceFromCenter(c1, 0, 1), Epsilon);
    }

    /**
     * Test of angle method, of class Circles.
     */
    @Test
    public void testAngle()
    {
        Circle c1 = new AbstractCircle(0, 0, 1);
        assertEquals(0, Math.toDegrees(Circles.angle(c1, 1, 0)), Epsilon);
        assertEquals(45, Math.toDegrees(Circles.angle(c1, 1, 1)), Epsilon);
        assertEquals(90, Math.toDegrees(Circles.angle(c1, 0, 1)), Epsilon);
        assertEquals(135, Math.toDegrees(Circles.angle(c1, -1, 1)), Epsilon);
        assertEquals(180, Math.toDegrees(Circles.angle(c1, -1, 0)), Epsilon);
        assertEquals(225, Math.toDegrees(Circles.angle(c1, -1, -1)), Epsilon);
        assertEquals(270, Math.toDegrees(Circles.angle(c1, 0, -1)), Epsilon);
        assertEquals(315, Math.toDegrees(Circles.angle(c1, 1, -1)), Epsilon);
    }
    
}
