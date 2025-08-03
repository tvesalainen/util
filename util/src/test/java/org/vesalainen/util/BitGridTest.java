/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.Point;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BitGridTest
{
    
    public BitGridTest()
    {
    }

    @Test
    public void testBounds()
    {
        BitGrid bg = new BitGrid(5, 8);
        bg.rect(1, 1, 3, 3, true);
        
        Rectangle bounds = bg.patternBounds();
        assertEquals(1, bounds.x);
        assertEquals(1, bounds.y);
        assertEquals(3, bounds.width);
        assertEquals(3, bounds.height);
        assertEquals(1.0F, bg.patternSquareness(), 1e-6);
        assertFalse(bg.patternOverflow());
        assertEquals(0.6, bg.patternLineCoverage(), 1e-6);
    }
    @Test
    public void testSquareness()
    {
        BitGrid bg = new BitGrid(5, 8);
        bg.line(1, 1, 5, true);
        bg.line(2, 2, 5, true);
        bg.line(3, 3, 5, true);
        assertTrue(1.0F > bg.patternSquareness());
        assertTrue(bg.patternOverflow());
        assertEquals(1.0, bg.patternLineCoverage(), 1e-6);
    }
}
