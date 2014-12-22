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

package org.vesalainen.navi;

import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.Circle;
import org.vesalainen.navi.SafeSector.Cursor;

/**
 *
 * @author Timo Vesalainen
 */
public class SafeSectorTest
{
    private static final double Epsilon = 1e-10;
    
    public SafeSectorTest()
    {
    }

    @Test
    public void testCenterCursor()
    {
        AbstractCircle c1 = new AbstractCircle(0, 0, 5);
        SafeSector ms = new SafeSector(c1);
        ms.set(0, 0);   // detach
        assertEquals(0, ms.getX(), Epsilon);
        assertEquals(0, ms.getY(), Epsilon);
        assertTrue(ms.isInside(-4.9, 0));
        c1.set(1, 1);
        assertEquals(1, c1.getX(), Epsilon);
        assertEquals(1, c1.getY(), Epsilon);
        assertEquals(0, ms.getX(), Epsilon);
        assertEquals(0, ms.getY(), Epsilon);
        Cursor cursor = ms.getCursor(0.1, -0.1, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(1, 1);
        assertFalse(ms.isInside(-4.9, 0));
        cursor = cursor.update(2, 2);
        assertTrue(ms.isInside(6.9, 2));
        assertEquals(2, ms.getX(), Epsilon);
        assertEquals(2, ms.getY(), Epsilon);
        assertEquals(1, c1.getX(), Epsilon);
        assertEquals(1, c1.getY(), Epsilon);
        cursor.ready(1.01, 1.02);   // attach
        assertEquals(1, ms.getX(), Epsilon);
        assertEquals(1, ms.getY(), Epsilon);
    }
    
    @Test
    public void testRadiusCursor()
    {
        AbstractCircle c1 = new AbstractCircle(0, 0, 5);
        SafeSector ms = new SafeSector(c1);
        Cursor cursor = ms.getCursor(-4.9, 0.1, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(-4, 0);
        assertTrue(ms.isInside(3.9, 0));
        assertFalse(ms.isInside(4.1, 0));
        assertEquals(4, ms.getRadius(), Epsilon);
        assertEquals(4, c1.getRadius(), Epsilon);
        cursor.ready(0, 6);
        assertEquals(6, ms.getRadius(), Epsilon);
        assertEquals(6, c1.getRadius(), Epsilon);
        assertTrue(ms.isInside(0, -5.9));
        assertTrue(ms.isInSector(0, -5.9));
        assertFalse(ms.isInside(0, 6.1));
    }
    
    @Test
    public void testRightCursor()
    {
        AbstractCircle c1 = new AbstractCircle(10, 10, 5);
        SafeSector ms = new SafeSector(c1);
        Cursor cursor = ms.getCursor(5, 10, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(5, 9.9);
        assertTrue(ms.isCircle());
        cursor = cursor.update(5, 9.4);
        assertFalse(ms.isCircle());
        cursor = cursor.update(10+5*Math.cos(Math.toRadians(225)), 10+5*Math.sin(Math.toRadians(225)));
        assertEquals(180, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(225, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(315, Math.toDegrees(ms.getAngle()), Epsilon);
        assertTrue(ms.isInSector(6, 10.1));
        assertFalse(ms.isInSector(6, 9.9));
        assertFalse(ms.isInSector(7.9, 8));
        assertTrue(ms.isInSector(8, 7.9));
        cursor.ready(10, 4);
        assertEquals(270, Math.toDegrees(ms.getRightAngle()), Epsilon);
        cursor = ms.getCursor(10.1, 5.2, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(14, 6);
        assertEquals(180, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(315, Math.toDegrees(ms.getRightAngle()), Epsilon);
    }
    
    @Test
    public void testLeftCursor()
    {
        AbstractCircle c1 = new AbstractCircle(10, 10, 5);
        SafeSector ms = new SafeSector(c1);
        Cursor cursor = ms.getCursor(5, 10, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(10+5*Math.cos(Math.toRadians(135)), 10+5*Math.sin(Math.toRadians(135)));
        assertEquals(180, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(135, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(315, Math.toDegrees(ms.getAngle()), Epsilon);
        assertFalse(ms.isInSector(6, 10.1));
        assertTrue(ms.isInSector(6, 9.9));
        assertFalse(ms.isInSector(7.9, 12));
        assertTrue(ms.isInSector(8, 12.1));
        cursor.ready(10, 4);
        assertEquals(270, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        cursor = ms.getCursor(9.9, 5.2, 0.5);
        assertNotNull(cursor);
        cursor.ready(14, 6);
        assertEquals(315, Math.toDegrees(ms.getLeftAngle()), Epsilon);
    }
    
    @Test
    public void testInnerCursor()
    {
        AbstractCircle c1 = new AbstractCircle(0, 0, 5);
        SafeSector ms = new SafeSector(c1);
        Cursor cursor = ms.getCursor(5, 0, 0.5);
        assertNotNull(cursor);
        cursor = cursor.update(0, 5);
        assertEquals(90, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(0, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(270, Math.toDegrees(ms.getAngle()), Epsilon);
        cursor = ms.getCursor(2.5, 0.2, 0.5);
        cursor = cursor.update(4.9, 0.2);
        Circle ic = ms.getInnerCircle();
        assertNotNull(ic);
        assertEquals(4.9, ic.getRadius(), 0.1);
        cursor = cursor.update(0.9, 0.2);
        assertEquals(0.9, ic.getRadius(), 0.1);
        assertFalse(ms.isCircle());
        cursor.ready(4.9, 0.2);
        assertTrue(ms.isCircle());
        ms.set(1, 1);
        assertEquals(ms.getX(), ic.getX(), Epsilon);
        assertEquals(ms.getY(), ic.getY(), Epsilon);
    }
    
    @Test
    public void testSetAngle()
    {
        AbstractCircle c1 = new AbstractCircle(0, 0, 5);
        SafeSector ms = new SafeSector(c1);
        ms.setRightAngle(Math.toRadians(90));
        ms.setLeftAngle(Math.toRadians(0));
        assertEquals(90, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(0, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(270, Math.toDegrees(ms.getAngle()), Epsilon);
    }
}
