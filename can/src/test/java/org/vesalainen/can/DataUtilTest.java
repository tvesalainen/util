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
import java.util.Random;
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
    public void testAsLongString()
    {
        assertEquals(8, DataUtil.length("1234567890abcdef"));
        long asLong = DataUtil.asLong("1234567890abcdef");
        assertEquals(0xefcdab9078563412L, DataUtil.asLong("1234567890abcdef"));
        assertEquals(0xab9078563412L, DataUtil.asLong("1234567890ab"));
        assertEquals(0xefcdab9078563412L, DataUtil.asLong("1234567890ABCDEF"));
        assertEquals(0xffffffffffffffffL, DataUtil.asLong("ffffffffffffffff"));
    }
    @Test
    public void testAsLongArray()
    {
        byte[] fromString = HexUtil.fromString("1234567890abcdef");
        assertEquals(0xefcdab9078563412L, DataUtil.asLong(fromString));
    }
    @Test
    public void testFromLong()
    {
        byte[] buf = new byte[8];
        DataUtil.fromLong(0x1234567890abcdefL, buf, 0, 8);
        assertEquals("EFCDAB9078563412", HexUtil.toString(buf));
        DataUtil.fromLong(0x12345678cdefL, buf, 0, 8);
        assertEquals("EFCD785634120000", HexUtil.toString(buf));
        Arrays.fill(buf, (byte)0);
        DataUtil.fromLong(0x1234567890abcdefL, 1, buf, 0, 7);
        assertEquals("CDAB907856341200", HexUtil.toString(buf));
    }
    @Test
    public void testGet()
    {
        assertEquals(0xEF, DataUtil.get(0x1234567890abcdefL, 0));
        assertEquals(0xCD, DataUtil.get(0x1234567890abcdefL, 1));
        assertEquals(255, DataUtil.get(0xffffffffffffffffL, 0));
    }    
    @Test
    public void testSet()
    {
        long l = 0;
        l = DataUtil.set(l, 0, 12);
        assertEquals(12, DataUtil.get(l, 0));
        assertEquals(0, DataUtil.get(l, 1));
        l = DataUtil.set(l, 5, 56);
        assertEquals(56, DataUtil.get(l, 5));
    }
    //@Test
    public void testRegression()
    {
        Random r = new Random(1234567L);
        byte[] buf = new byte[8];
        for (int ii=0;ii<10000;ii++)
        {
            long nextLong = r.nextLong();
            String hexString = Long.toHexString(nextLong);
            if (hexString.length()==8)
            {
                long asLong = DataUtil.asLong(hexString);
                DataUtil.fromLong(asLong, buf, 0, 8);
                String toString = HexUtil.toString(buf);
                int c = hexString.compareToIgnoreCase(toString);
                assertTrue(hexString.compareToIgnoreCase(toString)==0);
            }
        }
    }
}
