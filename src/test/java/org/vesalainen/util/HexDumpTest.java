/*
 * Copyright (C) 2016 tkv
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

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class HexDumpTest
{
    
    public HexDumpTest()
    {
    }

    @Test
    public void test1()
    {
        byte[] a = "qwerty\nasdfg\t\t\n1234567890".getBytes();
        String h = HexDump.toHex(a);
        System.err.println(h);
        assertEquals("    00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n" +
                "00: 71 77 65 72 74 79 0a 61 73 64 66 67 09 09 0a 31  q w e r t y . a s d f g . . . 1 \n" +
                "10: 32 33 34 35 36 37 38 39 30                       2 3 4 5 6 7 8 9 0 \n", h);
        Assert.assertArrayEquals(a, HexDump.fromHex(h));
    }
}
