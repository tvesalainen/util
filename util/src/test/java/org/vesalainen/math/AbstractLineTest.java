/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractLineTest
{
    private static final double Epsilon = 1e-10;
    public AbstractLineTest()
    {
    }

    @Test
    public void test1()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(1, 1);
        Point p3 = new AbstractPoint(2, 0);
        AbstractLine l1 = new AbstractLine(p1, p2);
        assertEquals(1, l1.getSlope(), Epsilon);
        assertEquals(0, l1.getA(), Epsilon);
        assertEquals(2, l1.getY(2), Epsilon);
        AbstractLine l1_s = new AbstractLine(1, p2);
        assertEquals(l1, l1_s);
        AbstractLine l2 = new AbstractLine(p2, p3);
        assertEquals(-1, l2.getSlope(), Epsilon);
        assertEquals(2, l2.getA(), Epsilon);
        assertEquals(-1, l2.getY(3), Epsilon);
        Point crossPoint = AbstractLine.crossPoint(l1, l2);
        assertEquals(1, crossPoint.getX(), Epsilon);
        assertEquals(1, crossPoint.getY(), Epsilon);
    }
    
    @Test
    public void test2()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(0, 1);
        Point p3 = new AbstractPoint(2, 0);
        AbstractLine l1 = new AbstractLine(p1, p2);
        assertTrue(Double.isInfinite(l1.getSlope()));
        assertEquals(0, l1.getA(), Epsilon);
        assertTrue(Double.isInfinite(l1.getY(0)));
        assertTrue(Double.isNaN(l1.getY(1)));
        AbstractLine l2 = new AbstractLine(p2, p3);
        Point crossPoint = AbstractLine.crossPoint(l1, l2);
        assertEquals(0, crossPoint.getX(), Epsilon);
        assertEquals(1, crossPoint.getY(), Epsilon);
    }
    
    @Test
    public void test3()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(1, 1);
        Point p3 = new AbstractPoint(1, 0);
        AbstractLine l1 = new AbstractLine(p1, p2);
        AbstractLine l2 = new AbstractLine(p2, p3);
        assertTrue(Double.isInfinite(l2.getSlope()));
        assertEquals(1, l2.getA(), Epsilon);
        assertTrue(Double.isInfinite(l2.getY(1)));
        assertTrue(Double.isNaN(l2.getY(2)));
        Point crossPoint = AbstractLine.crossPoint(l1, l2);
        assertEquals(1, crossPoint.getX(), Epsilon);
        assertEquals(1, crossPoint.getY(), Epsilon);
    }
    
    @Test
    public void test4()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(0, 1);
        Point p3 = new AbstractPoint(1, 0);
        Point p4 = new AbstractPoint(1, 1);
        AbstractLine l1 = new AbstractLine(p1, p2);
        AbstractLine l2 = new AbstractLine(p3, p4);
        Point crossPoint = AbstractLine.crossPoint(l1, l2);
        assertNull(crossPoint);
    }
    
    @Test
    public void test5()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(1, 1);
        Point p3 = new AbstractPoint(1, 0);
        Point p4 = new AbstractPoint(2, 1);
        AbstractLine l1 = new AbstractLine(p1, p2);
        AbstractLine l2 = new AbstractLine(p3, p4);
        Point crossPoint = AbstractLine.crossPoint(l1, l2);
        assertNull(crossPoint);
    }
    
    @Test
    public void test6()
    {
        Point p1 = new AbstractPoint(0, 0);
        Point p2 = new AbstractPoint(1, 1);
        AbstractLine l1 = new AbstractLine(p1, p2);
        Point crossPoint = AbstractLine.crossPoint(l1, l1);
        assertNull(crossPoint);
    }
    
}
