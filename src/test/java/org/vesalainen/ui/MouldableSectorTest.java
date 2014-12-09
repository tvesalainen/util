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

package org.vesalainen.ui;

import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.Circle;
import org.vesalainen.ui.MouldableSector.Cursor;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableSectorTest
{
    private static final double Epsilon = 1e-10;
    
    public MouldableSectorTest()
    {
    }

    @Test
    public void testCenterCursor()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        MouldableSector ms = new MouldableSector(c1);
        ms.detachCircle();
        assertEquals(0, ms.getX(), Epsilon);
        assertEquals(0, ms.getY(), Epsilon);
        Cursor cursor = ms.getCursor(0.1, -0.1);
        assertNotNull(cursor);
        cursor = cursor.update(1, 1);
        cursor = cursor.update(2, 2);
        assertEquals(2, ms.getX(), Epsilon);
        assertEquals(2, ms.getY(), Epsilon);
        ms.attachCircle();
        assertEquals(0, ms.getX(), Epsilon);
        assertEquals(0, ms.getY(), Epsilon);
    }
    
    @Test
    public void testRadiusCursor()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        MouldableSector ms = new MouldableSector(c1);
        Cursor cursor = ms.getCursor(-4.9, 0.1);
        assertNotNull(cursor);
        cursor = cursor.update(-4, 0);
        assertEquals(4, ms.getRadius(), Epsilon);
        cursor = cursor.update(0, 6);
        assertEquals(6, ms.getRadius(), Epsilon);
    }
    
    @Test
    public void testRightCursor()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        MouldableSector ms = new MouldableSector(c1);
        Cursor cursor = ms.getCursor(-5, 0);
        assertNotNull(cursor);
        cursor = cursor.update(5*Math.cos(Math.toRadians(225)), 5*Math.sin(Math.toRadians(225)));
        assertEquals(180, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(225, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(315, Math.toDegrees(ms.getAngle()), Epsilon);
        cursor = cursor.update(0, -6);
        assertEquals(270, Math.toDegrees(ms.getRightAngle()), Epsilon);
        cursor = ms.getCursor(0.1, -4.8);
        cursor = cursor.update(4, -4);
        assertEquals(315, Math.toDegrees(ms.getRightAngle()), Epsilon);
    }
    
    @Test
    public void testLeftCursor()
    {
        Circle c1 = new AbstractCircle(0, 0, 5);
        MouldableSector ms = new MouldableSector(c1);
        Cursor cursor = ms.getCursor(-5, 0);
        assertNotNull(cursor);
        cursor = cursor.update(5*Math.cos(Math.toRadians(135)), 5*Math.sin(Math.toRadians(135)));
        assertEquals(180, Math.toDegrees(ms.getRightAngle()), Epsilon);
        assertEquals(135, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        assertEquals(315, Math.toDegrees(ms.getAngle()), Epsilon);
        cursor = cursor.update(0, -6);
        assertEquals(270, Math.toDegrees(ms.getLeftAngle()), Epsilon);
        cursor = ms.getCursor(0.1, -4.8);
        cursor = cursor.update(4, -4);
        assertEquals(315, Math.toDegrees(ms.getLeftAngle()), Epsilon);
    }
    
}
