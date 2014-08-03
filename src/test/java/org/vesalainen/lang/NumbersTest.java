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
public class NumbersTest
{
    
    public NumbersTest()
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
     * Test of parseFloat method, of class Numbers.
     */
    @Test
    public void testParseFloat()
    {
        testEquals(0F, Numbers.parseFloat("0"));
        testEquals(123456.789F, Numbers.parseFloat("123456.789"));
        testEquals(123456.789F, Numbers.parseFloat("123456789E-3"));
        testEquals(123456.789F, Numbers.parseFloat("+123456.789"));
        testEquals(123456.789F, Numbers.parseFloat("+123456789E-3"));
        testEquals(-123456.789F, Numbers.parseFloat("-123456.789"));
        testEquals(-123456.789F, Numbers.parseFloat("-123456789E-3"));
        testEquals(1.0000001F, Numbers.parseFloat("1.00000017881393421514957253748434595763683319091796875001"));
        testEquals(1000000178813934215149572537484345F, Numbers.parseFloat("1000000178813934215149572537484345"));
        testEquals(1234F, Numbers.parseFloat("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Numbers.parseFloat("-123456789E-3.2");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseFloat("-123456789Ee-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseFloat("x123456789E-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseDouble method, of class Numbers.
     */
    @Test
    public void testParseDouble()
    {
        testEquals(0, Numbers.parseDouble("0"));
        testEquals(123456.789, Numbers.parseDouble("123456.789"));
        testEquals(123456.789, Numbers.parseDouble("123456789E-3"));
        testEquals(-123456.789, Numbers.parseDouble("-123456.789"));
        testEquals(-123456.789, Numbers.parseDouble("-123456789E-3"));
        testEquals(123456.789, Numbers.parseDouble("+123456.789"));
        testEquals(123456.789, Numbers.parseDouble("+123456789E-3"));
        double max = Double.MAX_VALUE;
        String maxs = Double.toString(max);
        testEquals(max, Numbers.parseDouble(maxs));
        double min = Double.MIN_VALUE;
        String mins = Double.toString(min);
        testEquals(min, Numbers.parseDouble(mins));
        testEquals(1.0000001788139342, Numbers.parseDouble("1.00000017881393421514957253748434595763683319091796875001"));
        testEquals(100000017881393421514957253748434595763683319091796875001.0, Numbers.parseDouble("100000017881393421514957253748434595763683319091796875001"));
        testEquals(1234, Numbers.parseDouble("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Numbers.parseDouble("-123456789E-3.2");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseDouble("-123456789Ee-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseDouble("x123456789E-3");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of Numbers.parseInt method, of class Numbers.
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
        assertEquals(0, Numbers.parseInt("0", 10));
        assertEquals(473, Numbers.parseInt("473", 10));
        assertEquals(42, Numbers.parseInt("+42", 10));
        assertEquals(0, Numbers.parseInt("-0", 10));
        assertEquals(-255, Numbers.parseInt("-FF", 16));
        assertEquals(102, Numbers.parseInt("1100110", 2));
        assertEquals(Integer.MAX_VALUE, Numbers.parseInt(maxDec, 10));
        assertEquals(Integer.MAX_VALUE, Numbers.parseInt(maxHex, 16));
        assertEquals(Integer.MAX_VALUE, Numbers.parseInt(maxOct, 8));
        assertEquals(Integer.MAX_VALUE, Numbers.parseInt(maxBin, 2));
        assertEquals(Integer.MIN_VALUE, Numbers.parseInt(minDec, 10));
        assertEquals(1234, Numbers.parseInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Numbers.parseInt("2147483648", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("-2147483649", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Numbers.parseInt("Kona", 27));
        assertEquals(126, Numbers.parseInt("01111110", -2));
        assertEquals(127, Numbers.parseInt("01111111", -2));
        assertEquals(2, Numbers.parseInt("00000010", -2));
        assertEquals(1, Numbers.parseInt("00000001", -2));
        assertEquals(0, Numbers.parseInt("00000000", -2));
        assertEquals(-1, Numbers.parseInt("11111111", -2));
        assertEquals(-2, Numbers.parseInt("11111110", -2));
        assertEquals(-1, Numbers.parseInt("11111111111111111111111111111111", -2));
        assertEquals(-1, Numbers.parseInt("1111111", -2));
        assertEquals(-2, Numbers.parseInt("1111110", -2));
        try
        {
            Numbers.parseInt("00000011111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("-11111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("+0000011111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseLong method, of class Numbers.
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
        assertEquals(0, Numbers.parseLong("0", 10));
        assertEquals(473, Numbers.parseLong("473", 10));
        assertEquals(42, Numbers.parseLong("+42", 10));
        assertEquals(0, Numbers.parseLong("-0", 10));
        assertEquals(-255, Numbers.parseLong("-FF", 16));
        assertEquals(102, Numbers.parseLong("1100110", 2));
        assertEquals(Long.MAX_VALUE, Numbers.parseLong(maxDec, 10));
        assertEquals(Long.MAX_VALUE, Numbers.parseLong(maxHex, 16));
        assertEquals(Long.MAX_VALUE, Numbers.parseLong(maxOct, 8));
        assertEquals(Long.MAX_VALUE, Numbers.parseLong(maxBin, 2));
        assertEquals(Long.MIN_VALUE, Numbers.parseLong(minDec, 10));
        assertEquals(1234, Numbers.parseLong("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Numbers.parseLong("9223372036854775808", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("-9223372036854775809", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Numbers.parseLong("Kona", 27));
        assertEquals(126, Numbers.parseLong("01111110", -2));
        assertEquals(127, Numbers.parseLong("01111111", -2));
        assertEquals(2, Numbers.parseLong("00000010", -2));
        assertEquals(1, Numbers.parseLong("00000001", -2));
        assertEquals(0, Numbers.parseLong("00000000", -2));
        assertEquals(-1, Numbers.parseLong("11111111", -2));
        assertEquals(-2, Numbers.parseLong("11111110", -2));
        assertEquals(-1, Numbers.parseLong("1111111111111111111111111111111111111111111111111111111111111111", -2));
        assertEquals(-1, Numbers.parseLong("1111111", -2));
        assertEquals(-2, Numbers.parseLong("1111110", -2));
        try
        {
            Numbers.parseLong("0000001111111111111111111111111111111111111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("+000001111111111111111111111111111111111111111111111111111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseShort method, of class Numbers.
     */
    @Test
    public void testParseShort_CharSequence_int()
    {
        String maxDec = Short.toString(Short.MAX_VALUE);
        String minDec = Short.toString(Short.MIN_VALUE);
        assertEquals(0, Numbers.parseShort("0", 10));
        assertEquals(473, Numbers.parseShort("473", 10));
        assertEquals(42, Numbers.parseShort("+42", 10));
        assertEquals(0, Numbers.parseShort("-0", 10));
        assertEquals(-255, Numbers.parseShort("-FF", 16));
        assertEquals(102, Numbers.parseShort("1100110", 2));
        assertEquals(Short.MAX_VALUE, Numbers.parseShort(maxDec, 10));
        assertEquals(Short.MIN_VALUE, Numbers.parseShort(minDec, 10));
        assertEquals(1234, Numbers.parseShort("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Numbers.parseShort("32768", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("-32769", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(15251, Numbers.parseShort("Kon", 27));
        assertEquals(126, Numbers.parseShort("01111110", -2));
        assertEquals(127, Numbers.parseShort("01111111", -2));
        assertEquals(2, Numbers.parseShort("00000010", -2));
        assertEquals(1, Numbers.parseShort("00000001", -2));
        assertEquals(0, Numbers.parseShort("00000000", -2));
        assertEquals(-1, Numbers.parseShort("11111111", -2));
        assertEquals(-2, Numbers.parseShort("11111110", -2));
        assertEquals(-1, Numbers.parseShort("1111111111111111", -2));
        assertEquals(-1, Numbers.parseShort("1111111", -2));
        assertEquals(-2, Numbers.parseShort("1111110", -2));
        try
        {
            Numbers.parseShort("0000001111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("-1111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("+000001111111111111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseByte method, of class Numbers.
     */
    @Test
    public void testParseByte_CharSequence_int()
    {
        String maxDec = Byte.toString(Byte.MAX_VALUE);
        String minDec = Byte.toString(Byte.MIN_VALUE);
        assertEquals(0, Numbers.parseByte("00000000000000000000000", 10));
        assertEquals(73, Numbers.parseByte("73", 10));
        assertEquals(42, Numbers.parseByte("+42", 10));
        assertEquals(0, Numbers.parseByte("-0", 10));
        assertEquals(-15, Numbers.parseByte("-F", 16));
        assertEquals(102, Numbers.parseByte("1100110", 2));
        assertEquals(Byte.MAX_VALUE, Numbers.parseByte(maxDec, 10));
        assertEquals(Byte.MIN_VALUE, Numbers.parseByte(minDec, 10));
        assertEquals(123, Numbers.parseByte("\ud835\udff7\ud835\udff8\ud835\udff9", 10));
        try
        {
            Numbers.parseByte("128", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("-129", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(20, Numbers.parseByte("K", 27));
        assertEquals(126, Numbers.parseByte("01111110", -2));
        assertEquals(127, Numbers.parseByte("01111111", -2));
        assertEquals(2, Numbers.parseByte("00000010", -2));
        assertEquals(1, Numbers.parseByte("00000001", -2));
        assertEquals(0, Numbers.parseByte("00000000", -2));
        assertEquals(-1, Numbers.parseByte("11111111", -2));
        assertEquals(-1, Numbers.parseByte("1111111", -2));
        assertEquals(-2, Numbers.parseByte("1111110", -2));
        try
        {
            Numbers.parseByte("00000011111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("-11111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("+0000011111111", -2);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseInt method, of class Numbers.
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
        assertEquals(0, Numbers.parseInt("0"));
        assertEquals(473, Numbers.parseInt("473"));
        assertEquals(42, Numbers.parseInt("+42"));
        assertEquals(0, Numbers.parseInt("-0"));
        assertEquals(Integer.MAX_VALUE, Numbers.parseInt(maxDec));
        assertEquals(Integer.MIN_VALUE, Numbers.parseInt(minDec));
        assertEquals(1234, Numbers.parseInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Numbers.parseInt("2147483648");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("-2147483649");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseInt("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseLong method, of class Numbers.
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
        assertEquals(0, Numbers.parseLong("0"));
        assertEquals(473, Numbers.parseLong("473"));
        assertEquals(42, Numbers.parseLong("+42"));
        assertEquals(0, Numbers.parseLong("-0"));
        assertEquals(Long.MAX_VALUE, Numbers.parseLong(maxDec));
        assertEquals(Long.MIN_VALUE, Numbers.parseLong(minDec));
        assertEquals(1234, Numbers.parseLong("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Numbers.parseLong("9223372036854775808");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("-9223372036854775809");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseLong("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseShort method, of class Numbers.
     */
    @Test
    public void testParseShort_CharSequence()
    {
        String maxDec = Short.toString(Short.MAX_VALUE);
        String minDec = Short.toString(Short.MIN_VALUE);
        assertEquals(0, Numbers.parseShort("0"));
        assertEquals(473, Numbers.parseShort("473"));
        assertEquals(42, Numbers.parseShort("+42"));
        assertEquals(0, Numbers.parseShort("-0"));
        assertEquals(Short.MAX_VALUE, Numbers.parseShort(maxDec));
        assertEquals(Short.MIN_VALUE, Numbers.parseShort(minDec));
        assertEquals(1234, Numbers.parseShort("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa"));
        try
        {
            Numbers.parseShort("32768");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("-32769");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseShort("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseByte method, of class Numbers.
     */
    @Test
    public void testParseByte_CharSequence()
    {
        String maxDec = Byte.toString(Byte.MAX_VALUE);
        String minDec = Byte.toString(Byte.MIN_VALUE);
        assertEquals(0, Numbers.parseByte("0"));
        assertEquals(73, Numbers.parseByte("73"));
        assertEquals(42, Numbers.parseByte("+42"));
        assertEquals(0, Numbers.parseByte("-0"));
        assertEquals(Byte.MAX_VALUE, Numbers.parseByte(maxDec));
        assertEquals(Byte.MIN_VALUE, Numbers.parseByte(minDec));
        assertEquals(123, Numbers.parseByte("\ud835\udff7\ud835\udff8\ud835\udff9"));
        try
        {
            Numbers.parseByte("128");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("-129");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseByte("Kona");
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
    }

    /**
     * Test of parseUnsignedInt method, of class Numbers.
     */
    @Test
    public void testParseUnsignedInt_CharSequence()
    {
    }

    /**
     * Test of parseUnsignedInt method, of class Numbers.
     */
    @Test
    public void testParseUnsignedInt_CharSequence_int()
    {
        String maxBin = Integer.toUnsignedString(-1, 2);
        String maxOct = Integer.toUnsignedString(-1, 8);
        String maxHex = Integer.toUnsignedString(-1, 16);
        String maxDec = Integer.toUnsignedString(-1, 10);
        assertEquals(0, Numbers.parseUnsignedInt("0", 10));
        assertEquals(473, Numbers.parseUnsignedInt("473", 10));
        assertEquals(42, Numbers.parseUnsignedInt("+42", 10));
        try
        {
            Numbers.parseUnsignedInt("-0", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseUnsignedInt("-FF", 16);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(102, Numbers.parseUnsignedInt("1100110", 2));
        assertEquals(-1, Numbers.parseUnsignedInt(maxDec, 10));
        assertEquals(-1, Numbers.parseUnsignedInt(maxHex, 16));
        assertEquals(-1, Numbers.parseUnsignedInt(maxOct, 8));
        assertEquals(-1, Numbers.parseUnsignedInt(maxBin, 2));
        assertEquals(1234, Numbers.parseUnsignedInt("\ud835\udff7\ud835\udff8\ud835\udff9\ud835\udffa", 10));
        try
        {
            Numbers.parseUnsignedInt("4294967296", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseUnsignedInt("99", 8);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        try
        {
            Numbers.parseUnsignedInt("Kona", 10);
            fail("should throw exception");
        }
        catch (NumberFormatException ex)
        {
        }
        assertEquals(411787, Numbers.parseUnsignedInt("Kona", 27));
    }

}
