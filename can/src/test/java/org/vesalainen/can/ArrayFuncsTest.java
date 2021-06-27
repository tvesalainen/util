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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.LongSupplier;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayFuncsTest
{
    private byte[] buf = new byte[]{
        (byte)0xf1, 
        (byte)0xf2, 
        (byte)0xf3, 
        (byte)0xf4, 
        (byte)0xf5, 
        (byte)0xf6, 
        (byte)0xf7, 
        (byte)0xf8,
        (byte)0xf9, 
        (byte)0xfa, 
        (byte)0xfb, 
        (byte)0xfc, 
        (byte)0xfd, 
        (byte)0xfe, 
        (byte)0xff
    };
    
    public ArrayFuncsTest()
    {
    }

    @Test
    public void testAligned()
    {
        for (int ll=8;ll<64;ll+=8)
        {
            test(8, ll, true, true);
            test(8, ll, true, false);
            test(8, ll, false, true);
            test(8, ll, false, false);
        }
    }
    private void test(int offset, int length, boolean bigEndian, boolean signed)
    {
        LongSupplier lsb = ArrayFuncs.getSupplier(offset, length, bigEndian, signed, buf);
        LongSupplier exp = ArrayFuncs.getAlignedSupplier(offset, length, bigEndian, signed, buf);
        assertEquals("o="+offset+" l="+length+" be="+bigEndian+" s="+signed, exp.getAsLong(), lsb.getAsLong());
    }
    @Test
    public void testLongUnsignedLittleEndianX()
    {
        LongSupplier lubx = ArrayFuncs.getLongUnsignedLittleEndianX(8, 16, buf);
        long exp = 0xf3f2;
        assertEquals(exp, lubx.getAsLong());
    }
    @Test
    public void testLongSignedLittleEndianX()
    {
        LongSupplier lubx = ArrayFuncs.getLongSignedLittleEndianX(8, 16, buf);
        long exp = (short)0xf3f2;
        assertEquals(exp, lubx.getAsLong());
    }
    @Test
    public void testLongUnsignedBigEndianX()
    {
        LongSupplier lubx = ArrayFuncs.getLongUnsignedBigEndianX(8, 16, buf);
        long exp = 0xf2f3;
        assertEquals(exp, lubx.getAsLong());
    }
    @Test
    public void testLongSignedBigEndianX()
    {
        LongSupplier lsbx = ArrayFuncs.getLongSignedBigEndianX(8, 16, buf);
        long exp = (short)0xf2f3;
        assertEquals(exp, lsbx.getAsLong());
    }
    @Test
    public void testLongSignedBigEndian()
    {
        for (int ll=1;ll<9;ll++)
        {
            LongSupplier lsb = ArrayFuncs.getLongSignedBigEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs.getLongSignedBigEndianX(0, 8*ll, buf);
            assertEquals("ll="+ll, exp.getAsLong(), lsb.getAsLong());
        }
    }
    @Test
    public void testLongUnsignedBigEndian()
    {
        for (int ll=1;ll<8;ll++)
        {
            LongSupplier lsb = ArrayFuncs.getLongUnsignedBigEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs.getLongUnsignedBigEndianX(0, 8*ll, buf);
            System.err.println(Long.toHexString(lsb.getAsLong()));
            System.err.println(Long.toHexString(exp.getAsLong()));
            assertEquals("ll="+ll, exp.getAsLong(), lsb.getAsLong());
        }
    }
    @Test
    public void testLongSignedLittleEndian()
    {
        for (int ll=1;ll<9;ll++)
        {
            LongSupplier lsb = ArrayFuncs.getLongSignedLittleEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs.getLongSignedLittleEndianX(0, 8*ll, buf);
            assertEquals("ll="+ll, exp.getAsLong(), lsb.getAsLong());
        }
    }
    @Test
    public void testLongUnsignedLittleEndian()
    {
        for (int ll=1;ll<8;ll++)
        {
            LongSupplier lsb = ArrayFuncs.getLongUnsignedLittleEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs.getLongUnsignedLittleEndianX(0, 8*ll, buf);
            assertEquals("ll="+ll, exp.getAsLong(), lsb.getAsLong());
        }
    }
    
}
