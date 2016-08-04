/*
 * Copyright (C) 2015 tkv
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
 * @author tkv
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
        ba.set(10, false);
        assertFalse(ba.isSet(10));
        ba.setAll(true);
        assertTrue(ba.isSet(10));
        ba.setAll(false);
        assertFalse(ba.isSet(10));
    }
    
}
