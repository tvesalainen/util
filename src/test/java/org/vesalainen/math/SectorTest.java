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
public class SectorTest
{
    private static final double Epsilon = 1e-10;
    
    public SectorTest()
    {
    }

    @Test
    public void testGetAngle1()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(80), Math.toRadians(10));
        assertEquals(70, Math.toDegrees(sector.getAngle()), Epsilon);
    }
    @Test
    public void testGetAngle2()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(180), Math.toRadians(10));
        assertEquals(170, Math.toDegrees(sector.getAngle()), Epsilon);
    }

    @Test
    public void testGetAngle3()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(280), Math.toRadians(10));
        assertEquals(270, Math.toDegrees(sector.getAngle()), Epsilon);
    }

    @Test
    public void testGetAngle4()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(10), Math.toRadians(350));
        assertEquals(20, Math.toDegrees(sector.getAngle()), Epsilon);
    }

    @Test
    public void testIsInside1()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(280), Math.toRadians(170));
        assertTrue(sector.isInside(-2, -2));
        assertTrue(sector.isInside(-4, 0));
        assertTrue(sector.isInside(0, -4));
        assertFalse(sector.isInside(-4, -4));
        assertFalse(sector.isInside(1, 1));
        assertFalse(sector.isInside(-1, 1));
        assertFalse(sector.isInside(1, -1));
    }

    @Test
    public void testCorners()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        AbstractSector sector = new AbstractSector(c1, Math.toRadians(120), Math.toRadians(60));
        assertEquals(2.5, sector.getRightX(), Epsilon);
        assertEquals(5*Math.sin(Math.toRadians(60)), sector.getRightY(), Epsilon);
        assertEquals(-2.5, sector.getLeftX(), Epsilon);
        assertEquals(5*Math.sin(Math.toRadians(60)), sector.getLeftY(), Epsilon);
    }

}
