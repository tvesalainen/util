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

package org.vesalainen.util.math;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class PolygonTest
{
    
    public PolygonTest()
    {
    }

    /**
     * Test of isHit method, of class Polygon.
     */
    @Test
    public void testIsHit()
    {
        DenseMatrix64F x = new DenseMatrix64F(6, 2, true,
                1, 1,
                3, 3,
                6, 1,
                3, 6,
                1, 6,
                1, 1
        );
        Polygon p = new Polygon(x);
        assertTrue(p.isHit(2, 3));
        assertTrue(p.isHit(5, 2));
        assertFalse(p.isHit(3, 2));
        assertFalse(p.isHit(0, 0));
    }

    /**
     * Test of isRawHit method, of class Polygon.
     */
    @Test
    public void testIsRawHit()
    {
        DenseMatrix64F x = new DenseMatrix64F(6, 2, true,
                1, 1,
                3, 3,
                6, 1,
                3, 6,
                1, 6,
                1, 1
        );
        assertTrue(Polygon.isRawHit(x, 2, 3));
        assertTrue(Polygon.isRawHit(x, 5, 2));
        assertFalse(Polygon.isRawHit(x, 3, 2));
        assertFalse(Polygon.isRawHit(x, 0, 0));
    }

    /**
     * Test of createConvexPolygon method, of class Polygon.
     */
    @Test
    public void testCreateConvexPolygon()
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
        Polygon p = Polygon.createConvexPolygon(x);
        assertTrue(p.isHit(2, 4));
        assertTrue(p.isHit(3, 4));
        assertTrue(p.isHit(3, 6));
        assertTrue(p.isHit(3, 7));
        assertTrue(p.isHit(4, 3));
        assertTrue(p.isHit(4, 7));
        assertTrue(p.isHit(6, 7));
        assertTrue(p.isHit(7, 8));
        assertTrue(p.isHit(8, 2));
        assertTrue(p.isHit(8, 6));
        assertTrue(p.isHit(9, 3));
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
        assertFalse(p.isHit(0, 0));
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isHit(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
    public void testCreateConvexPolygon0()
    {
        DenseMatrix64F x = new DenseMatrix64F(6, 2, true,
                1, 1,
                3.1, 3,
                6, 1,
                3, 6,
                1, 6,
                1, 1
        );
        Polygon p = Polygon.createConvexPolygon(x);
        assertTrue(p.isHit(2, 3));
        assertTrue(p.isHit(5, 2));
        assertTrue(p.isHit(3, 2));
        assertFalse(p.isHit(0, 0));
        for (int r=0;r<x.numRows;r++)
        {
            assertTrue(
                    p.isHit(x.data[2*r], x.data[2*r+1]) ||
                    p.isVertex(x.data[2*r], x.data[2*r+1])
            );
        }
    }
    
}
