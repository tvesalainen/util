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

import static java.lang.Integer.min;
import java.util.Arrays;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.LongReference;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayFuncsTest
{

    private byte[] buf = new byte[]
    {
        (byte) 0xf1,
        (byte) 0xf2,
        (byte) 0xf3,
        (byte) 0xf4,
        (byte) 0xf5,
        (byte) 0xf6,
        (byte) 0xf7,
        (byte) 0xf8,
        (byte) 0xf9,
        (byte) 0xfa,
        (byte) 0xfb,
        (byte) 0xfc,
        (byte) 0xfd,
        (byte) 0xfe,
        (byte) 0xff
    };

    public ArrayFuncsTest()
    {
    }

    @Test
    public void testWriteLong()
    {
        byte[] buf = new byte[8];
        LongReference i = new LongReference(123);
        boolean bigEndian=false;
        boolean signed=false;
        testWriteLong(0, 8, bigEndian, signed, i::getValue, buf);
        testWriteLong(8, 8, bigEndian, signed, i::getValue, buf);
        testWriteLong(16, 8, bigEndian, signed, i::getValue, buf);
        testWriteLong(8, 15, bigEndian, signed, i::getValue, buf);
        testWriteLong(11, 15, bigEndian, signed, i::getValue, buf);
        testWriteLong(23, 27, bigEndian, signed, i::getValue, buf);
    }
    private void testWriteLong(int offset, int length, boolean bigEndian, boolean signed, LongSupplier i, byte[] buf)
    {
        Arrays.fill(buf, (byte)0x0);
        Runnable longWriter = ArrayFuncs.getLongWriter(offset, length, bigEndian, signed, i, buf);
        LongSupplier longSupplier = ArrayFuncs.getLongSupplier(offset, length, bigEndian, signed, buf);
        longWriter.run();
        assertEquals("o="+offset+" l="+length+" e="+bigEndian+" s="+signed, i.getAsLong(), longSupplier.getAsLong());
    }
    @Test
    public void testWriteInt()
    {
        byte[] buf = new byte[8];
        IntReference i = new IntReference(123);
        boolean bigEndian=false;
        boolean signed=true;
        testWriteInt(0, 8, bigEndian, signed, i::getValue, buf);
        testWriteInt(8, 8, bigEndian, signed, i::getValue, buf);
        testWriteInt(16, 8, bigEndian, signed, i::getValue, buf);
        testWriteInt(8, 15, bigEndian, signed, i::getValue, buf);
        testWriteInt(11, 15, bigEndian, signed, i::getValue, buf);
        testWriteInt(23, 27, bigEndian, signed, i::getValue, buf);
    }
    private void testWriteInt(int offset, int length, boolean bigEndian, boolean signed, IntSupplier i, byte[] buf)
    {
        Arrays.fill(buf, (byte)0x0);
        Runnable intWriter = ArrayFuncs.getIntWriter(offset, length, bigEndian, signed, i, buf);
        IntSupplier intSupplier = ArrayFuncs.getIntSupplier(offset, length, bigEndian, signed, buf);
        intWriter.run();
        assertEquals("o="+offset+" l="+length+" e="+bigEndian+" s="+signed, i.getAsInt(), intSupplier.getAsInt());
    }
    @Test
    public void testLE()
    {
        byte[] arr = ArrayFuncs.createLittleEndian(2, 16);
    }
    @Test
    public void testIIRIS()
    {
        byte[] arr = HexUtil.fromString("44482452");
        int ser = get6Bit(arr[0]);
        assertEquals(1, ser>>>2);
        ser &= 3;
        ser <<= 6;
        ser |= get6Bit(arr[1]);
        ser <<= 6;
        ser |= get6Bit(arr[2]);
        ser <<= 6;
        ser |= get6Bit(arr[3]);
        assertEquals(35090, ser);
    }
    private int get6Bit(byte cc)
    {
        if (cc > 64 && cc <= 95)
        {
            return cc - 64;
        }
        else
        {
            if (cc >= 32 && cc <= 63)
            {
                return cc;
            }
            else
            {
                throw new IllegalArgumentException(cc+" cannot be encoded");
            }
        }
    }
    @Test
    public void testWETA()
    {
        byte[] arr = HexUtil.fromString("48435B51");
        find(14033, false, false, arr);
    }
    @Test
    public void test00()
    {
        byte[] arr = HexUtil.fromString("4652434C");
        find(598220, false, false, arr);
    }
    public int find(int val, boolean bigEndian, boolean signed, byte[] arr)
    {
        int bits = 32-Integer.numberOfLeadingZeros(val);
        int len = arr.length*8;
        int max = len-bits;
        for (int ii=0;ii<max;ii++)
        {
            IntSupplier is1 = ArrayFuncs.getIntSupplier(ii, bits, bigEndian, signed, arr);
            int asInt = is1.getAsInt();
            if (asInt == val)
            {
                return ii;
            }
        }
        return -1;
    }
    @Test
    public void testGNSSPositionData()
    {
        byte[] arr = HexUtil.fromString("4A7949B069932F0040AAC50EAA0AFB00005D9BF7983118000000000000000023FC0C45008A00120C0000010000000000000000");
        IntSupplier is1 = ArrayFuncs.getIntSupplier(248, 4, false, false, arr);
        assertEquals(3, is1.getAsInt());
        IntSupplier is2 = ArrayFuncs.getIntSupplier(252, 4, false, false, arr);
        assertEquals(2, is2.getAsInt());
    }

    @Test
    public void testAlloc()
    {
        assertEquals(2, ArrayFuncs.dim(8, 16));
        assertEquals(3, ArrayFuncs.dim(7, 16));
        assertEquals(1, ArrayFuncs.dim(0, 3));
    }

    @Test
    public void testVesselHeading()
    {
        byte[] arr = HexUtil.fromString("b2b7dd0000110dfc");
        IntSupplier is = ArrayFuncs.getIntSupplier(56, 2, false, false, arr);
        assertEquals(0, is.getAsInt());
    }

    @Test
    public void testWindData()
    {
        byte[] arr = HexUtil.fromString("4c 99 01 2e 96 fa ff ff".replace(" ", ""));
        IntSupplier is = ArrayFuncs.getIntSupplier(40, 3, false, false, arr);
        assertEquals(2, is.getAsInt());
    }

    @Test
    public void testAlignedInt()
    {
        for (int ll = 8; ll < 32; ll += 8)
        {
            testInt(8, ll, true, true);
            testInt(8, ll, true, false);
            testInt(8, ll, false, true);
            testInt(8, ll, false, false);
        }
    }

    private void testInt(int offset, int length, boolean bigEndian, boolean signed)
    {
        IntSupplier lsb = ArrayFuncs.getIntSupplier(offset, length, bigEndian, signed, buf);
        IntSupplier exp = ArrayFuncs0.getAlignedIntSupplier(offset, length, bigEndian, signed, buf);
        assertEquals("o=" + offset + " l=" + length + " be=" + bigEndian + " s=" + signed, exp.getAsInt(), lsb.getAsInt());
    }

    @Test
    public void testAlignedLong()
    {
        for (int ll = 8; ll < 64; ll += 8)
        {
            testLong(8, ll, true, true);
            testLong(8, ll, true, false);
            testLong(8, ll, false, true);
            testLong(8, ll, false, false);
        }
    }

    private void testLong(int offset, int length, boolean bigEndian, boolean signed)
    {
        LongSupplier lsb = ArrayFuncs.getLongSupplier(offset, length, bigEndian, signed, buf);
        LongSupplier exp = ArrayFuncs0.getAlignedLongSupplier(offset, length, bigEndian, signed, buf);
        assertEquals("o=" + offset + " l=" + length + " be=" + bigEndian + " s=" + signed, exp.getAsLong(), lsb.getAsLong());
    }

    @Test
    public void testLongUnsignedLittleEndianX()
    {
        LongSupplier lubx = ArrayFuncs0.getLongUnsignedLittleEndianX(8, 16, buf);
        long exp = 0xf3f2;
        assertEquals(exp, lubx.getAsLong());
    }

    @Test
    public void testLongSignedLittleEndianX()
    {
        LongSupplier lubx = ArrayFuncs0.getLongSignedLittleEndianX(8, 16, buf);
        long exp = (short) 0xf3f2;
        assertEquals(exp, lubx.getAsLong());
    }

    @Test
    public void testLongUnsignedBigEndianX()
    {
        LongSupplier lubx = ArrayFuncs0.getLongUnsignedBigEndianX(8, 16, buf);
        long exp = 0xf2f3;
        assertEquals(exp, lubx.getAsLong());
    }

    @Test
    public void testLongSignedBigEndianX()
    {
        LongSupplier lsbx = ArrayFuncs0.getLongSignedBigEndianX(8, 16, buf);
        long exp = (short) 0xf2f3;
        assertEquals(exp, lsbx.getAsLong());
    }

    @Test
    public void testLongSignedBigEndian()
    {
        for (int ll = 1; ll < 9; ll++)
        {
            LongSupplier lsb = ArrayFuncs0.getLongSignedBigEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs0.getLongSignedBigEndianX(0, 8 * ll, buf);
            assertEquals("ll=" + ll, exp.getAsLong(), lsb.getAsLong());
        }
    }

    @Test
    public void testLongUnsignedBigEndian()
    {
        for (int ll = 1; ll < 8; ll++)
        {
            LongSupplier lsb = ArrayFuncs0.getLongUnsignedBigEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs0.getLongUnsignedBigEndianX(0, 8 * ll, buf);
            System.err.println(Long.toHexString(lsb.getAsLong()));
            System.err.println(Long.toHexString(exp.getAsLong()));
            assertEquals("ll=" + ll, exp.getAsLong(), lsb.getAsLong());
        }
    }

    @Test
    public void testLongSignedLittleEndian()
    {
        for (int ll = 1; ll < 9; ll++)
        {
            LongSupplier lsb = ArrayFuncs0.getLongSignedLittleEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs0.getLongSignedLittleEndianX(0, 8 * ll, buf);
            assertEquals("ll=" + ll, exp.getAsLong(), lsb.getAsLong());
        }
    }

    @Test
    public void testLongUnsignedLittleEndian()
    {
        for (int ll = 1; ll < 8; ll++)
        {
            LongSupplier lsb = ArrayFuncs0.getLongUnsignedLittleEndian(0, ll, buf);
            LongSupplier exp = ArrayFuncs0.getLongUnsignedLittleEndianX(0, 8 * ll, buf);
            assertEquals("ll=" + ll, exp.getAsLong(), lsb.getAsLong());
        }
    }

}
