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
import org.vesalainen.math.matrix.DoubleMatrix;

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
     * Test of isInside method, of class Polygon.
     */
    @Test
    public void testIsHit()
    {
        DoubleMatrix x = new DoubleMatrix(6, 2, true,
                1, 1,
                3, 3,
                6, 1,
                3, 6,
                1, 6,
                1, 1
        );
        Polygon p = new Polygon(x);
        assertTrue(p.isInside(2, 3));
        assertTrue(p.isInside(5, 2));
        assertFalse(p.isInside(3, 2));
        assertFalse(p.isInside(0, 0));
    }

    /**
     * Test of isRawHit method, of class Polygon.
     */
    @Test
    public void testIsRawHit()
    {
        DoubleMatrix x = new DoubleMatrix(6, 2, true,
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

}
