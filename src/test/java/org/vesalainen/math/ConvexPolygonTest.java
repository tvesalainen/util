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
    public void testSlopeComp1()
    {
        for (int d1=0;d1<400;d1++)
        {
            int d2 = d1 + 5;
            double a1 = Math.toRadians(d1);
            double a2 = Math.toRadians(d2);
            double x1 = Math.cos(a1);
            double y1 = Math.sin(a1);
            double x2 = Math.cos(a2);
            double y2 = Math.sin(a2);
            assertTrue(ConvexPolygon.slopeComp(x2, y2, x1, y1)>0);
        }
    }
    @Test
    public void testSlopeComp2()
    {
        for (int d1=0;d1<400;d1++)
        {
            int d2 = d1 + 179;
            double a1 = Math.toRadians(d1);
            double a2 = Math.toRadians(d2);
            double x1 = Math.cos(a1);
            double y1 = Math.sin(a1);
            double x2 = Math.cos(a2);
            double y2 = Math.sin(a2);
            assertTrue(ConvexPolygon.slopeComp(x2, y2, x1, y1)>0);
        }
    }
    @Test
    public void testSlopeComp3()
    {
        for (int d1=0;d1<400;d1++)
        {
            int d2 = d1 + 181;
            double a1 = Math.toRadians(d1);
            double a2 = Math.toRadians(d2);
            double x1 = Math.cos(a1);
            double y1 = Math.sin(a1);
            double x2 = Math.cos(a2);
            double y2 = Math.sin(a2);
            assertTrue(ConvexPolygon.slopeComp(x2, y2, x1, y1)<0);
        }
    }
    @Test
    public void testIsConvex1()
    {
        DenseMatrix64F x = new DenseMatrix64F(4, 2, true,
                1, 1,
                2, 2,
                3, 1,
                2, 3
        );
        assertFalse(ConvexPolygon.isConvex(x));
    }
    
    @Test
    public void testIsConvex2()
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
        assertFalse(ConvexPolygon.isConvex(x));
    }
    
    @Test
    public void testGetOuterBoundary()
    {
        DenseMatrix64F o = new DenseMatrix64F(0, 2);
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
        System.err.println(p);
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isInside(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
        p.getOuterBoundary(3, -1, o);
        assertEquals(4, o.numRows);
        int idx = 0;
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(6, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        
        p.getOuterBoundary(6, 6, o);
        assertEquals(5, o.numRows);
        idx = 0;
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(6, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        
        p.getOuterBoundary(3, 8, o);
        assertEquals(4, o.numRows);
        idx = 0;
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        
        p.getOuterBoundary(-1, 3, o);
        assertEquals(3, o.numRows);
        idx = 0;
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(5, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(6, o.data[idx++], Epsilon);
        
    }
    
    @Test
    public void testGetOuterBoundary2()
    {
        DenseMatrix64F o = new DenseMatrix64F(0, 2);
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
        assertEquals(2, o.numRows);
        int idx = 0;
        assertEquals(3, o.data[idx++], Epsilon);
        assertEquals(2, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
        assertEquals(1, o.data[idx++], Epsilon);
    }        
}
