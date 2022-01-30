/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DataUtilTest
{
    
    public DataUtilTest()
    {
    }

    @Test
    public void testAsLong()
    {
        assertEquals(8, DataUtil.length("1234567890abcdef"));
        assertEquals(0x1234567890abcdefL, DataUtil.asLong("1234567890abcdef"));
        assertEquals(0x1234567890ab0000L, DataUtil.asLong("1234567890ab"));
        assertEquals(0x1234567890abcdefL, DataUtil.asLong("1234567890ABCDEF"));
        assertEquals(0xffffffffffffffffL, DataUtil.asLong("ffffffffffffffff"));
    }
    @Test
    public void testFromLong()
    {
        byte[] buf = new byte[8];
        DataUtil.fromLong(0x1234567890abcdefL, buf, 0, 8);
        assertEquals("1234567890ABCDEF", HexUtil.toString(buf));
        DataUtil.fromLong(0x12345678cdefL, buf, 0, 8);
        assertEquals("000012345678CDEF", HexUtil.toString(buf));
        Arrays.fill(buf, (byte)0);
        DataUtil.fromLong(0x1234567890abcdefL, 1, buf, 0, 7);
        assertEquals("34567890ABCDEF00", HexUtil.toString(buf));
    }
    @Test
    public void testGet()
    {
        assertEquals(0x12, DataUtil.get(0x1234567890abcdefL, 0));
        assertEquals(0x34, DataUtil.get(0x1234567890abcdefL, 1));
        assertEquals(255, DataUtil.get(0xffffffffffffffffL, 0));
    }    
}
