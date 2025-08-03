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
package org.vesalainen.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BitArrayTest
{
    
    public BitArrayTest()
    {
    }

    @Test
    public void test1()
    {
        BitArray ba = new BitArray(100);
        ba.set(10, true);
        assertFalse(ba.isSet(9));
        assertTrue(ba.isSet(10));
        assertFalse(ba.isSet(11));
        assertEquals(10, ba.first());
        assertEquals(10, ba.last());
        ba.set(10, false);
        assertFalse(ba.isSet(10));
        ba.setAll(true);
        assertTrue(ba.isSet(10));
        assertTrue(ba.any());
        ba.setAll(false);
        assertFalse(ba.isSet(10));
        assertFalse(ba.any());
    }
    @Test
    public void testAny()
    {
        BitArray ba = new BitArray(9);
        ba.set(8, true);
        assertTrue(ba.any());
        ba.setAll(true);
        ba.set(8, false);
        assertTrue(ba.any());
        ba.setAll(false);
        ba.set(7, true);
        assertTrue(ba.any());
    }    
    @Test
    public void testAnd()
    {
        BitArray ba1 = new BitArray(20);
        ba1.set(3, 4, true);
        BitArray ba2 = new BitArray(20);
        assertFalse(ba1.and(ba2));
        ba2.set(6, 4, true);
        assertTrue(ba1.and(ba2));
    }
    @Test
    public void testForEach()
    {
        BitArray ba1 = new BitArray(20);
        ba1.set(3, 4, true);
        assertEquals(18, ba1.stream().sum());
    }
}
