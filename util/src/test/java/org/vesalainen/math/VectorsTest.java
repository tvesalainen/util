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
public class VectorsTest
{
    
    public VectorsTest()
    {
    }

    /**
     * Test of isClockwise method, of class Vectors.
     */
    @Test
    public void testIsClockwise()
    {
        assertTrue(Vectors.isClockwise(0, 1, 10, 0));
        assertTrue(Vectors.isClockwise(0, 1, 0.1, -1));
        assertTrue(Vectors.isClockwise(-1, 1, 1, 1));
        assertFalse(Vectors.isClockwise(-1, 1, 1, -1));
        assertTrue(Vectors.isClockwise(-1, -0.1, -1, 0));
        assertTrue(Vectors.isClockwise(-1, -1, -1, -0.1));
        assertTrue(Vectors.isClockwise(-3, -3, -4, -0.1));
    }
    
}
