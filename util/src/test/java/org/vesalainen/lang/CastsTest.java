/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.lang;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.lang.Casts.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CastsTest
{
    
    public CastsTest()
    {
    }

    @Test
    public void test1()
    {
        assertEquals(255, castUnsignedInt((byte)-1));
        assertEquals(65535, castUnsignedInt((short)-1));
        assertEquals(255, castUnsignedLong((byte)-1));
        assertEquals(65535, castUnsignedLong((short)-1));
        assertEquals(4294967295L, castUnsignedLong((int)-1));
        assertEquals(0x80000000L, castUnsignedLong((int)0x80000000));
        assertEquals(-32348, castUnsignedShort(33188L));
    }
    
}
