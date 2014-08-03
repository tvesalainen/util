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

package org.vesalainen.lang;

import static org.junit.Assert.*;
import org.junit.Test;
import static org.vesalainen.junit.Assertions.*;

/**
 *
 * @author Timo Vesalainen
 */
public class PrimitivesTest
{
    
    public PrimitivesTest()
    {
    }

    //@Test
    public void test()
    {
        for (int cp = Character.MIN_CODE_POINT;cp < Character.MAX_CODE_POINT;cp++)
        {
            if (Character.isDigit(cp))
            {
                System.err.println(Character.getName(cp)+"="+Character.digit(cp, 10)+" \\u"+Integer.toHexString(Character.highSurrogate(cp))+"\\u"+Integer.toHexString(Character.lowSurrogate(cp)));
            }
        }
    }
    /**
     * Test of parseFloat method, of class Primitives.
     */
    @Test
    public void testParseFloat()
    {
        testEquals(0F, Primitives.parseFloat("0"));
        testEquals(123456.789F, Primitives.parseFloat("123456.789"));
        testEquals(123456.789F, Primitives.parseFloat("123456789E-3"));
        testEquals(123456.789F, Primitives.parseFloat("+123456.789"));
        testEquals(123456.789F, Primitives.parseFloat("+123456789E-3"));
        testEquals(-123456.789F, Primitives.parseFloat("-123456.789"));
        testEquals(-123456.789F, Primitives.parseFloat("-123456789E-3"));
        testEquals(1.0000001F, Primitives.parseFloat("1.00000017881393421514957253748434595763683319091796875001"));
        testEquals(1000000178813934215149572537484345F, Primitives.parseFloat("1000000178813934215149572537484345"));
        testEquals(1234F, Primitives.parseFloat("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Primitives.parseFloat("-123456789E-3.2");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseFloat("-123456789Ee-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseFloat("x123456789E-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseDouble method, of class Primitives.
     */
    @Test
    public void testParseDouble()
    {
        testEquals(0, Primitives.parseDouble("0"));
        testEquals(123456.789, Primitives.parseDouble("123456.789"));
        testEquals(123456.789, Primitives.parseDouble("123456789E-3"));
        testEquals(-123456.789, Primitives.parseDouble("-123456.789"));
        testEquals(-123456.789, Primitives.parseDouble("-123456789E-3"));
        testEquals(123456.789, Primitives.parseDouble("+123456.789"));
        testEquals(123456.789, Primitives.parseDouble("+123456789E-3"));
        double max = Double.MAX_VALUE;
        String maxs = Double.toString(max);
        testEquals(max, Primitives.parseDouble(maxs));
        double min = Double.MIN_VALUE;
        String mins = Double.toString(min);
        testEquals(min, Primitives.parseDouble(mins));
        testEquals(1.0000001788139342, Primitives.parseDouble("1.00000017881393421514957253748434595763683319091796875001"));
        testEquals(100000017881393421514957253748434595763683319091796875001.0, Primitives.parseDouble("100000017881393421514957253748434595763683319091796875001"));
        testEquals(1234, Primitives.parseDouble("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Primitives.parseDouble("-123456789E-3.2");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseDouble("-123456789Ee-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseDouble("x123456789E-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of Primitives.parseInt method, of class Primitives.
     */
    @Test
    public void testParseInt_CharSequence_int()
    {
        String maxBin = Integer.toBinaryString(Integer.MAX_VALUE);
        String maxOct = Integer.toOctalString(Integer.MAX_VALUE);
        String maxHex = Integer.toHexString(Integer.MAX_VALUE);
        String maxDec = Integer.toString(Integer.MAX_VALUE);
        String minBin = Integer.toBinaryString(Integer.MIN_VALUE);
        String minOct = Integer.toOctalString(Integer.MIN_VALUE);
        String minHex = Integer.toHexString(Integer.MIN_VALUE);
        String minDec = Integer.toString(Integer.MIN_VALUE);
        assertEquals(0, Primitives.parseInt("0", 10));
        assertEquals(473, Primitives.parseInt("473", 10));
        assertEquals(42, Primitives.parseInt("+42", 10));
        assertEquals(0, Primitives.parseInt("-0", 10));
        assertEquals(-255, Primitives.parseInt("-FF", 16));
        assertEquals(102, Primitives.parseInt("1100110", 2));
        assertEquals(Integer.MAX_VALUE, Primitives.parseInt(maxDec, 10));
        assertEquals(Integer.MAX_VALUE, Primitives.parseInt(maxHex, 16));
        assertEquals(Integer.MAX_VALUE, Primitives.parseInt(maxOct, 8));
        assertEquals(Integer.MAX_VALUE, Primitives.parseInt(maxBin, 2));
        assertEquals(Integer.MIN_VALUE, Primitives.parseInt(minDec, 10));
        assertEquals(1234, Primitives.parseInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Primitives.parseInt("2147483648", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("-2147483649", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Primitives.parseInt("Kona", 27));
        assertEquals(126, Primitives.parseInt("01111110", -2));
        assertEquals(127, Primitives.parseInt("01111111", -2));
        assertEquals(2, Primitives.parseInt("00000010", -2));
        assertEquals(1, Primitives.parseInt("00000001", -2));
        assertEquals(0, Primitives.parseInt("00000000", -2));
        assertEquals(-1, Primitives.parseInt("11111111", -2));
        assertEquals(-2, Primitives.parseInt("11111110", -2));
        assertEquals(-1, Primitives.parseInt("11111111111111111111111111111111", -2));
        assertEquals(-1, Primitives.parseInt("1111111", -2));
        assertEquals(-2, Primitives.parseInt("1111110", -2));
        try
        {
            Primitives.parseInt("00000011111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("-11111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("+0000011111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseLong method, of class Primitives.
     */
    @Test
    public void testParseLong_CharSequence_int()
    {
        String maxBin = Long.toBinaryString(Long.MAX_VALUE);
        String maxOct = Long.toOctalString(Long.MAX_VALUE);
        String maxHex = Long.toHexString(Long.MAX_VALUE);
        String maxDec = Long.toString(Long.MAX_VALUE);
        String minBin = Long.toBinaryString(Long.MIN_VALUE);
        String minOct = Long.toOctalString(Long.MIN_VALUE);
        String minHex = Long.toHexString(Long.MIN_VALUE);
        String minDec = Long.toString(Long.MIN_VALUE);
        assertEquals(0, Primitives.parseLong("0", 10));
        assertEquals(473, Primitives.parseLong("473", 10));
        assertEquals(42, Primitives.parseLong("+42", 10));
        assertEquals(0, Primitives.parseLong("-0", 10));
        assertEquals(-255, Primitives.parseLong("-FF", 16));
        assertEquals(102, Primitives.parseLong("1100110", 2));
        assertEquals(Long.MAX_VALUE, Primitives.parseLong(maxDec, 10));
        assertEquals(Long.MAX_VALUE, Primitives.parseLong(maxHex, 16));
        assertEquals(Long.MAX_VALUE, Primitives.parseLong(maxOct, 8));
        assertEquals(Long.MAX_VALUE, Primitives.parseLong(maxBin, 2));
        assertEquals(Long.MIN_VALUE, Primitives.parseLong(minDec, 10));
        assertEquals(1234, Primitives.parseLong("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Primitives.parseLong("9223372036854775808", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("-9223372036854775809", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Primitives.parseLong("Kona", 27));
        assertEquals(126, Primitives.parseLong("01111110", -2));
        assertEquals(127, Primitives.parseLong("01111111", -2));
        assertEquals(2, Primitives.parseLong("00000010", -2));
        assertEquals(1, Primitives.parseLong("00000001", -2));
        assertEquals(0, Primitives.parseLong("00000000", -2));
        assertEquals(-1, Primitives.parseLong("11111111", -2));
        assertEquals(-2, Primitives.parseLong("11111110", -2));
        assertEquals(-1, Primitives.parseLong("1111111111111111111111111111111111111111111111111111111111111111", -2));
        assertEquals(-1, Primitives.parseLong("1111111", -2));
        assertEquals(-2, Primitives.parseLong("1111110", -2));
        try
        {
            Primitives.parseLong("0000001111111111111111111111111111111111111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("+000001111111111111111111111111111111111111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseShort method, of class Primitives.
     */
    @Test
    public void testParseShort_CharSequence_int()
    {
        String maxDec = Short.toString(Short.MAX_VALUE);
        String minDec = Short.toString(Short.MIN_VALUE);
        assertEquals(0, Primitives.parseShort("0", 10));
        assertEquals(473, Primitives.parseShort("473", 10));
        assertEquals(42, Primitives.parseShort("+42", 10));
        assertEquals(0, Primitives.parseShort("-0", 10));
        assertEquals(-255, Primitives.parseShort("-FF", 16));
        assertEquals(102, Primitives.parseShort("1100110", 2));
        assertEquals(Short.MAX_VALUE, Primitives.parseShort(maxDec, 10));
        assertEquals(Short.MIN_VALUE, Primitives.parseShort(minDec, 10));
        assertEquals(1234, Primitives.parseShort("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Primitives.parseShort("32768", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("-32769", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(15251, Primitives.parseShort("Kon", 27));
        assertEquals(126, Primitives.parseShort("01111110", -2));
        assertEquals(127, Primitives.parseShort("01111111", -2));
        assertEquals(2, Primitives.parseShort("00000010", -2));
        assertEquals(1, Primitives.parseShort("00000001", -2));
        assertEquals(0, Primitives.parseShort("00000000", -2));
        assertEquals(-1, Primitives.parseShort("11111111", -2));
        assertEquals(-2, Primitives.parseShort("11111110", -2));
        assertEquals(-1, Primitives.parseShort("1111111111111111", -2));
        assertEquals(-1, Primitives.parseShort("1111111", -2));
        assertEquals(-2, Primitives.parseShort("1111110", -2));
        try
        {
            Primitives.parseShort("0000001111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("-1111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("+000001111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseByte method, of class Primitives.
     */
    @Test
    public void testParseByte_CharSequence_int()
    {
        String maxDec = Byte.toString(Byte.MAX_VALUE);
        String minDec = Byte.toString(Byte.MIN_VALUE);
        assertEquals(0, Primitives.parseByte("00000000000000000000000", 10));
        assertEquals(73, Primitives.parseByte("73", 10));
        assertEquals(42, Primitives.parseByte("+42", 10));
        assertEquals(0, Primitives.parseByte("-0", 10));
        assertEquals(-15, Primitives.parseByte("-F", 16));
        assertEquals(102, Primitives.parseByte("1100110", 2));
        assertEquals(Byte.MAX_VALUE, Primitives.parseByte(maxDec, 10));
        assertEquals(Byte.MIN_VALUE, Primitives.parseByte(minDec, 10));
        assertEquals(123, Primitives.parseByte("\ud835\udff7\ud835\udff8\ud835\udff9", 10));
        try
        {
            Primitives.parseByte("128", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("-129", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(20, Primitives.parseByte("K", 27));
        assertEquals(126, Primitives.parseByte("01111110", -2));
        assertEquals(127, Primitives.parseByte("01111111", -2));
        assertEquals(2, Primitives.parseByte("00000010", -2));
        assertEquals(1, Primitives.parseByte("00000001", -2));
        assertEquals(0, Primitives.parseByte("00000000", -2));
        assertEquals(-1, Primitives.parseByte("11111111", -2));
        assertEquals(-1, Primitives.parseByte("1111111", -2));
        assertEquals(-2, Primitives.parseByte("1111110", -2));
        try
        {
            Primitives.parseByte("00000011111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("-11111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("+0000011111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseInt method, of class Primitives.
     */
    @Test
    public void testParseInt_CharSequence()
    {
        String maxBin = Integer.toBinaryString(Integer.MAX_VALUE);
        String maxOct = Integer.toOctalString(Integer.MAX_VALUE);
        String maxHex = Integer.toHexString(Integer.MAX_VALUE);
        String maxDec = Integer.toString(Integer.MAX_VALUE);
        String minBin = Integer.toBinaryString(Integer.MIN_VALUE);
        String minOct = Integer.toOctalString(Integer.MIN_VALUE);
        String minHex = Integer.toHexString(Integer.MIN_VALUE);
        String minDec = Integer.toString(Integer.MIN_VALUE);
        assertEquals(0, Primitives.parseInt("0"));
        assertEquals(473, Primitives.parseInt("473"));
        assertEquals(42, Primitives.parseInt("+42"));
        assertEquals(0, Primitives.parseInt("-0"));
        assertEquals(Integer.MAX_VALUE, Primitives.parseInt(maxDec));
        assertEquals(Integer.MIN_VALUE, Primitives.parseInt(minDec));
        assertEquals(1234, Primitives.parseInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Primitives.parseInt("2147483648");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("-2147483649");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseInt("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseLong method, of class Primitives.
     */
    @Test
    public void testParseLong_CharSequence()
    {
        String maxBin = Long.toBinaryString(Long.MAX_VALUE);
        String maxOct = Long.toOctalString(Long.MAX_VALUE);
        String maxHex = Long.toHexString(Long.MAX_VALUE);
        String maxDec = Long.toString(Long.MAX_VALUE);
        String minBin = Long.toBinaryString(Long.MIN_VALUE);
        String minOct = Long.toOctalString(Long.MIN_VALUE);
        String minHex = Long.toHexString(Long.MIN_VALUE);
        String minDec = Long.toString(Long.MIN_VALUE);
        assertEquals(0, Primitives.parseLong("0"));
        assertEquals(473, Primitives.parseLong("473"));
        assertEquals(42, Primitives.parseLong("+42"));
        assertEquals(0, Primitives.parseLong("-0"));
        assertEquals(Long.MAX_VALUE, Primitives.parseLong(maxDec));
        assertEquals(Long.MIN_VALUE, Primitives.parseLong(minDec));
        assertEquals(1234, Primitives.parseLong("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Primitives.parseLong("9223372036854775808");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("-9223372036854775809");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseLong("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseShort method, of class Primitives.
     */
    @Test
    public void testParseShort_CharSequence()
    {
        String maxDec = Short.toString(Short.MAX_VALUE);
        String minDec = Short.toString(Short.MIN_VALUE);
        assertEquals(0, Primitives.parseShort("0"));
        assertEquals(473, Primitives.parseShort("473"));
        assertEquals(42, Primitives.parseShort("+42"));
        assertEquals(0, Primitives.parseShort("-0"));
        assertEquals(Short.MAX_VALUE, Primitives.parseShort(maxDec));
        assertEquals(Short.MIN_VALUE, Primitives.parseShort(minDec));
        assertEquals(1234, Primitives.parseShort("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Primitives.parseShort("32768");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("-32769");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseShort("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseByte method, of class Primitives.
     */
    @Test
    public void testParseByte_CharSequence()
    {
        String maxDec = Byte.toString(Byte.MAX_VALUE);
        String minDec = Byte.toString(Byte.MIN_VALUE);
        assertEquals(0, Primitives.parseByte("0"));
        assertEquals(73, Primitives.parseByte("73"));
        assertEquals(42, Primitives.parseByte("+42"));
        assertEquals(0, Primitives.parseByte("-0"));
        assertEquals(Byte.MAX_VALUE, Primitives.parseByte(maxDec));
        assertEquals(Byte.MIN_VALUE, Primitives.parseByte(minDec));
        assertEquals(123, Primitives.parseByte("\ud835\udff7\ud835\udff8\ud835\udff9"));
        try
        {
            Primitives.parseByte("128");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("-129");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseByte("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseUnsignedInt method, of class Primitives.
     */
    @Test
    public void testParseUnsignedInt_CharSequence()
    {
    }

    /**
     * Test of parseUnsignedInt method, of class Primitives.
     */
    @Test
    public void testParseUnsignedInt_CharSequence_int()
    {
        String maxBin = Integer.toUnsignedString(-1, 2);
        String maxOct = Integer.toUnsignedString(-1, 8);
        String maxHex = Integer.toUnsignedString(-1, 16);
        String maxDec = Integer.toUnsignedString(-1, 10);
        assertEquals(0, Primitives.parseUnsignedInt("0", 10));
        assertEquals(473, Primitives.parseUnsignedInt("473", 10));
        assertEquals(42, Primitives.parseUnsignedInt("+42", 10));
        try
        {
            Primitives.parseUnsignedInt("-0", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseUnsignedInt("-FF", 16);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(102, Primitives.parseUnsignedInt("1100110", 2));
        assertEquals(-1, Primitives.parseUnsignedInt(maxDec, 10));
        assertEquals(-1, Primitives.parseUnsignedInt(maxHex, 16));
        assertEquals(-1, Primitives.parseUnsignedInt(maxOct, 8));
        assertEquals(-1, Primitives.parseUnsignedInt(maxBin, 2));
        assertEquals(1234, Primitives.parseUnsignedInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Primitives.parseUnsignedInt("4294967296", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseUnsignedInt("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Primitives.parseUnsignedInt("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Primitives.parseUnsignedInt("Kona", 27));
    }

}
