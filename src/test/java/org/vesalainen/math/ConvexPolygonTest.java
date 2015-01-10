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

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class ConvexPolygonTest
{
    private static final double Epsilon = 1e-6;
    
    public ConvexPolygonTest()
    {
    }

    @Test
    public void testAddPointAligned()
    {
        ConvexPolygon cp = new ConvexPolygon();
        assertTrue(cp.addPoint(4, 4));
        assertEquals(1, cp.getCount());
        assertFalse(cp.addPoint(4, 4));
        assertEquals(1, cp.getCount());
        assertTrue(cp.addPoint(4, 6));
        assertEquals(2, cp.getCount());
        assertFalse(cp.addPoint(4, 5));
        assertEquals(2, cp.getCount());
        assertTrue(cp.addPoint(4, 7));
        assertEquals(2, cp.getCount());
        assertFalse(cp.contains(4, 6));
        assertTrue(cp.contains(4, 7));
        assertTrue(cp.addPoint(4, 3));
        assertEquals(2, cp.getCount());
        assertFalse(cp.contains(4, 4));
        assertTrue(cp.contains(4, 3));
    }
    @Test
    public void testAddPointUnaligned()
    {
        ConvexPolygon cp = new ConvexPolygon();
        
        assertTrue(cp.addPoint(4, 4));
        assertEquals(1, cp.getCount());
        assertTrue(cp.isConvex());
        
        assertFalse(cp.addPoint(4, 4));
        assertEquals(1, cp.getCount());
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(6, 6));
        assertEquals(2, cp.getCount());
        assertTrue(cp.isConvex());
        
        assertFalse(cp.addPoint(5, 5));
        assertEquals(2, cp.getCount());
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(7, 7));
        assertEquals(2, cp.getCount());
        assertFalse(cp.contains(6, 6));
        assertTrue(cp.contains(7, 7));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(3, 3));
        assertEquals(2, cp.getCount());
        assertFalse(cp.contains(4, 4));
        assertTrue(cp.contains(3, 3));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(4, 6));
        assertEquals(3, cp.getCount());
        assertTrue(cp.contains(4, 6));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(7, 5));
        assertEquals(4, cp.getCount());
        assertTrue(cp.contains(7, 5));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(8, 4));
        assertEquals(4, cp.getCount());
        assertTrue(cp.contains(8, 4));
        assertFalse(cp.contains(7, 5));
        assertTrue(cp.isConvex());
        
        assertFalse(cp.addPoint(6, 4));
        assertEquals(4, cp.getCount());
        assertFalse(cp.contains(6, 4));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(5, 7));
        assertEquals(5, cp.getCount());
        assertTrue(cp.contains(5, 7));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(4, 8));
        assertEquals(4, cp.getCount());
        assertTrue(cp.contains(4, 8));
        assertFalse(cp.contains(4, 6));
        assertFalse(cp.contains(5, 7));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(3, 6));
        assertEquals(5, cp.getCount());
        assertTrue(cp.contains(3, 6));
        assertTrue(cp.isConvex());
        
        assertFalse(cp.addPoint(3, 5));
        assertEquals(5, cp.getCount());
        assertFalse(cp.contains(3, 5));
        assertTrue(cp.isConvex());
        
        assertTrue(cp.addPoint(6, 3));
        assertEquals(6, cp.getCount());
        assertTrue(cp.contains(6, 3));
        assertTrue(cp.isConvex());
        
        assertFalse(cp.addPoint(5, 3));
        assertEquals(6, cp.getCount());
        assertFalse(cp.contains(5, 3));
        assertTrue(cp.isConvex());
        
    }
    /**
     * Test of createConvexPolygon method, of class Polygon.
     */
    @Test
    public void testCreateConvexPolygon1()
    {
        DenseMatrix64F x = new DenseMatrix64F(22, 2, true,
                1, 6,
                2, 3,
                2, 4,
                3, 4,
                3, 6,
                3, 7,
                3, 8,
                4, 2,
                4, 3,
                4, 7,
                5, 9,
                6, 1,
                6, 7,
                7, 8,
                8, 2,
                8, 4,
                8, 6,
                8, 8,
                9, 2,
                9, 3,
                9, 7,
                10, 5
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        System.err.println(p);
        assertTrue(p.isInside(2, 4));
        assertTrue(p.isInside(3, 4));
        assertTrue(p.isInside(3, 6));
        assertTrue(p.isInside(3, 7));
        assertTrue(p.isInside(4, 3));
        assertTrue(p.isInside(4, 7));
        assertTrue(p.isInside(6, 7));
        assertTrue(p.isInside(7, 8));
        assertTrue(p.isInside(8, 2));
        assertTrue(p.isInside(8, 6));
        assertTrue(p.isInside(9, 3));
        assertTrue(p.isVertex(10, 5));
        assertTrue(p.isVertex(9, 7));
        assertTrue(p.isVertex(8, 8));
        assertTrue(p.isVertex(5, 9));
        assertTrue(p.isVertex(3, 8));
        assertTrue(p.isVertex(1, 6));
        assertTrue(p.isVertex(2, 3));
        assertTrue(p.isVertex(4, 2));
        assertTrue(p.isVertex(6, 1));
        assertTrue(p.isVertex(9, 2));
        assertFalse(p.isInside(0, 0));
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
    @Test
    public void testCreateConvexPolygon2()
    {
        DenseMatrix64F x = new DenseMatrix64F(6, 2, true,
                1, 1,
                3.1, 3,
                6, 1,
                3, 6,
                1, 6,
                1, 1
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        assertTrue(p.isInside(2, 3));
        assertTrue(p.isInside(5, 2));
        assertTrue(p.isInside(3, 2));
        assertFalse(p.isInside(0, 0));
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
    @Test
    public void testCreateConvexPolygon3()
    {
        DenseMatrix64F x = new DenseMatrix64F(10, 2, true,
                1, 1,
                2, 1,
                4, 1,
                4, 4,
                4, 6,
                3, 6,
                1, 6,
                1, 3,
                2, 4,
                3, 3
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        System.err.println(p);
        assertTrue(p.isInside(2, 3));
        assertTrue(p.isInside(3, 5));
        assertTrue(p.isInside(3, 2));
        assertFalse(p.isInside(0, 0));
    }
    
    @Test
    public void testCreateConvexPolygon4()
    {
        DenseMatrix64F x = new DenseMatrix64F(7, 2, true,
                1, 1,
                2, 3,
                3, 2,
                3, 5,
                4, 2,
                4, 3,
                6, 1
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        System.err.println(p);
        assertTrue(p.isInside(2, 2));
        assertTrue(p.isInside(3, 4));
        assertTrue(p.isInside(3, 2));
        assertFalse(p.isInside(0, 0));
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
    @Test
    public void testCreateConvexPolygon5()
    {
        DenseMatrix64F x = new DenseMatrix64F(6, 2, true,
                1, 4,
                2, 3,
                3, 1,
                3, 4,
                4, 3,
                5, 4
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        System.err.println(p);
        assertTrue(p.isInside(2, 3));
        assertTrue(p.isInside(3, 3));
        assertTrue(p.isInside(3, 2));
        assertFalse(p.isInside(2, 2));
        assertFalse(p.isInside(0, 0));
    }
    
    @Test
    public void testCreateConvexPolygon6()
    {
        DenseMatrix64F x = new DenseMatrix64F(19, 2, true,
            -13.602548516397329, 28.13049850582962,
            -13.602548516397329, 28.13049850582962,
            -13.602548516397329, 28.13049850582962,
            -13.602548516397329, 28.13049850582962,
            -13.60259081510616, 28.13061761313844,
            -13.602592162607337, 28.130479910567498,
            -13.602600802260106, 28.13047585242093,
            -13.60271992555684, 28.130470810548374,
            -13.602678515714459, 28.130427399977204,
            -13.602667551381089, 28.13042491869222,
            -13.60263220645123, 28.130458223726567,
            -13.602630471397239, 28.13044835586738,
            -13.602630175975841, 28.13043926538994,
            -13.602621258296706, 28.13043826093284,
            -13.602620722251533, 28.13045360204717,
            -13.602618111976572, 28.13046265824846,
            -13.60261474127407, 28.13044636129253,
            -13.602610946252684, 28.130470377542295,
            -13.602587236295879, 28.130489450596553 
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        System.err.println(p);
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
    @Test
    public void testCreateConvexPolygon7()
    {
        DenseMatrix64F x = new DenseMatrix64F(7, 2, true,
                2, 465,
                1000, 15,
                938, 245,
                832, 595,
                215, 986,
                0, 876,
                1000, 15
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
    }
    
    @Test
    public void testGetOuterBoundary()
    {
        ConvexPolygon o = new ConvexPolygon();
        DenseMatrix64F x = new DenseMatrix64F(7, 2, true,
                1, 2,
                2, 1,
                3, 2,
                5, 3,
                4, 4,
                3, 6,
                2, 5
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        assertTrue(p.isVertex(1, 2));
        assertTrue(p.isVertex(2, 1));
        assertTrue(p.isVertex(5, 3));
        assertTrue(p.isVertex(3, 6));
        assertTrue(p.isVertex(2, 5));
        assertTrue(p.isInside(3, 3));
        assertTrue(p.isInside(4, 4));
        p.getOuterBoundary(3, -1, o);
        assertEquals(4, o.getCount());
        assertTrue(p.isVertex(5, 3));
        assertTrue(p.isVertex(3, 6));
        assertTrue(p.isVertex(2, 5));
        assertTrue(p.isVertex(1, 2));
        
        p.getOuterBoundary(6, 6, o);
        assertEquals(5, o.getCount());
        assertTrue(p.isVertex(3, 6));
        assertTrue(p.isVertex(2, 5));
        assertTrue(p.isVertex(1, 2));
        assertTrue(p.isVertex(2, 1));
        assertTrue(p.isVertex(5, 3));
        
        p.getOuterBoundary(3, 8, o);
        assertEquals(4, o.getCount());
        assertTrue(p.isVertex(2, 5));
        assertTrue(p.isVertex(1, 2));
        assertTrue(p.isVertex(2, 1));
        assertTrue(p.isVertex(5, 3));
        
        p.getOuterBoundary(-1, 3, o);
        assertEquals(3, o.getCount());
        assertTrue(p.isVertex(2, 1));
        assertTrue(p.isVertex(5, 3));
        assertTrue(p.isVertex(3, 6));
        
    }
    
    @Test
    public void testGetOuterBoundary2()
    {
        ConvexPolygon o = new ConvexPolygon();
        DenseMatrix64F x = new DenseMatrix64F(2, 2, true,
                1, 1,
                3, 2
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
        p.getOuterBoundary(0, 0, o);
        assertEquals(2, o.getCount());
        assertTrue(p.isVertex(3, 2));
        assertTrue(p.isVertex(1, 1));
    }        
    @Test
    public void testdistanceFromLine()
    {
        assertEquals(1, ConvexPolygon.distanceFromLine(1, 1, 0, 0, 0, 1), Epsilon);
        assertEquals(1, ConvexPolygon.distanceFromLine(1, 1, 0, 0, 1, 0), Epsilon);
        assertEquals(0, ConvexPolygon.distanceFromLine(1, 1, 0, 0, 1, 1), Epsilon);
        assertEquals(Math.sqrt(2), ConvexPolygon.distanceFromLine(1, 1, 0, 0, 1, -1), Epsilon);
    }
    @Test
    public void testGetMinimumDistance()
    {
        DenseMatrix64F x = new DenseMatrix64F(7, 2, true,
                1, 2,
                2, 1,
                3, 2,
                5, 3,
                4, 4,
                3, 6,
                2, 5
        );
        ConvexPolygon p = ConvexPolygon.createConvexPolygon(x);
        assertEquals(Math.sqrt(2)/2, p.getMinimumDistance(2, 2), Epsilon);
        assertEquals(ConvexPolygon.distanceFromLine(3, 4, 5, 3, 3, 6), p.getMinimumDistance(3, 4), Epsilon);
    }
}
