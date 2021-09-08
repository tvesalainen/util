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

import java.nio.ByteBuffer;
import org.junit.Assert;
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
        testEquals(123456.789F, Primitives.parseFloat("123123456.789123", 3, 13));
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
        testEquals(123456.789, Primitives.parseDouble("123123456.789123", 3, 13));
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
        assertEquals(473, Primitives.parseInt("123473123", 3, 6));
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
        assertEquals(473, Primitives.parseLong("123473123", 3, 6));
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
        assertEquals(473, Primitives.parseShort("123473123", 3, 6));
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
        assertEquals(73, Primitives.parseByte("12373123", 3, 5));
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
    /*
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
    */

    /**
     * Test of parseChar method, of class Primitives.
     */
    @Test
    public void testParseChar()
    {
        assertEquals('A', Primitives.parseChar("A"));
        assertEquals('A', Primitives.parseChar("ZAC", 1, 2));
        try
        {
            Primitives.parseChar("qwerty");
            fail("should throw exception");
        }
        catch (IllegalArgumentException ex)
        {
        }
        try
        {
            Primitives.parseChar("");
            fail("should throw exception");
        }
        catch (IllegalArgumentException ex)
        {
        }
    }

    /**
     * Test of parseBoolean method, of class Primitives.
     */
    @Test
    public void testParseBoolean_CharSequence()
    {
        assertEquals(true, Primitives.parseBoolean("True"));
        assertEquals(true, Primitives.parseBoolean("FalseTrueFalse", 5, 9));
        assertEquals(true, Primitives.parseBoolean("true"));
        assertEquals(false, Primitives.parseBoolean("qwerty"));
    }

    /**
     * Test of parseBoolean method, of class Primitives.
     */
    @Test
    public void testParseBoolean_CharSequence_int()
    {
        assertEquals(true, Primitives.parseBoolean("1", 2));
        assertEquals(true, Primitives.parseBoolean("010", 2, 1, 2));
        assertEquals(false, Primitives.parseBoolean("0", 2));
        try
        {
            Primitives.parseBoolean("q", 2);
            fail("should throw exception");
        }
        catch (IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void testextra()
    {
        assertEquals(4100.0013F, Primitives.parseFloat("$GPRMC,065010,A,1555.6283,N,04100.0013,W,4.9,265,241215,17,W*43", 28, 38), 1e-15);
        
    }
    @Test
    public void testSignum()
    {
        assertEquals(-1 , Primitives.signum(-1234));
        assertEquals(0 , Primitives.signum(0));
        assertEquals(1 , Primitives.signum(1234));
        
        assertEquals(-1L , Primitives.signum(-1234L));
        assertEquals(0L , Primitives.signum(0L));
        assertEquals(1L , Primitives.signum(1234L));
    }
    @Test
    public void testWriteInt()
    {
        byte[] expArray = new byte[4];
        int expInt = 0xffbbccdd;
        ByteBuffer bb = ByteBuffer.wrap(expArray);
        bb.putInt(expInt);
        byte[] got = Primitives.writeInt(expInt);
        assertArrayEquals(expArray, got);
        assertEquals(expInt, Primitives.readInt(got));
    }
    @Test
    public void testWriteLong()
    {
        byte[] expArray = new byte[8];
        long expLong = 0xaabbccddeeff1122L;
        ByteBuffer bb = ByteBuffer.wrap(expArray);
        bb.putLong(expLong);
        byte[] got = Primitives.writeLong(expLong);
        assertArrayEquals(expArray, got);
        long gotLong = Primitives.readLong(got);
        assertEquals(expLong, gotLong);
    }
    @Test
    public void testWriteDouble()
    {
        byte[] expArray = new byte[8];
        double expDouble = 1.2345678e-23;
        ByteBuffer bb = ByteBuffer.wrap(expArray);
        bb.putDouble(expDouble);
        byte[] got = Primitives.writeDouble(expDouble);
        assertArrayEquals(expArray, got);
        double gotDouble = Primitives.readDouble(got);
        assertEquals(expDouble, gotDouble, 1e-10);
    }
    @Test
    public void testWriteFloat()
    {
        byte[] expArray = new byte[4];
        float expFloat = 1.2345678e-23F;
        ByteBuffer bb = ByteBuffer.wrap(expArray);
        bb.putFloat(expFloat);
        byte[] got = Primitives.writeFloat(expFloat);
        assertArrayEquals(expArray, got);
        float gotDouble = Primitives.readFloat(got);
        assertEquals(expFloat, gotDouble, 1e-10);
    }
    @Test
    public void testWriteShort()
    {
        byte[] expArray = new byte[2];
        short expShort = (short) 0xffbb;
        ByteBuffer bb = ByteBuffer.wrap(expArray);
        bb.putShort(expShort);
        byte[] got = Primitives.writeShort(expShort);
        assertArrayEquals(expArray, got);
        assertEquals(expShort, Primitives.readShort(got));
    }
    @Test
    public void testHex()
    {
        assertEquals(0xab, Primitives.parseShort("0xab"));
        assertEquals(0xabcde, Primitives.parseInt("0xabcde"));
        assertEquals(0xabcde, Primitives.parseLong("0xabcde"));
    }
    @Test
    public void testBinary()
    {
        assertEquals(0b0111, Primitives.parseShort("0b0111"));
        assertEquals(0b01110101, Primitives.parseInt("0b01110101"));
        assertEquals(0b01110101, Primitives.parseLong("0b01110101"));
    }
    @Test
    public void testToDigits()
    {
        assertEquals("00001234", Primitives.toDigits(1234, 8, 10).collect(StringBuilder::new, (s,c)->s.append((char)c), null).toString());
        assertEquals("9223372036854775807", Primitives.toDigits(Long.MAX_VALUE, 19, 10).collect(StringBuilder::new, (s,c)->s.append((char)c), null).toString());
        assertEquals("007fffffff", Primitives.toDigits(Integer.MAX_VALUE, 10, 16).collect(StringBuilder::new, (s,c)->s.append((char)c), null).toString());
        try
        {
            String str = Primitives.toDigits(100, 2, 10).collect(StringBuilder::new, (s,c)->s.append((char)c), null).toString();
            fail("IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
        }
        try
        {
            String str = Primitives.toDigits(100, 1000, 10).collect(StringBuilder::new, (s,c)->s.append((char)c), null).toString();
            fail("IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    @Test
    public void testIsDecimalDigit()
    {
        assertTrue("0123456789-+".chars().allMatch(Primitives::isDecimalDigit));
        assertTrue("abcdfe".chars().noneMatch(Primitives::isDecimalDigit));
    }
    @Test
    public void testIsFloatDigit()
    {
        assertTrue("0123456789.-+".chars().allMatch(Primitives::isFloatDigit));
        assertTrue("abcdef,".chars().noneMatch(Primitives::isFloatDigit));
    }
    @Test
    public void testIsScientificDigit()
    {
        assertTrue("0123456789.-+eE".chars().allMatch(Primitives::isScientificDigit));
        assertTrue("abcdfABCDF,".chars().noneMatch(Primitives::isScientificDigit));
    }
    @Test
    public void testIsOctalDigit()
    {
        assertTrue("01234567".chars().allMatch(Primitives::isOctalDigit));
        assertTrue("89abcdefABCDEF.,+-".chars().noneMatch(Primitives::isOctalDigit));
    }
    @Test
    public void testIsHexDigit()
    {
        assertTrue("0123456789abcdefABCDEF".chars().allMatch(Primitives::isHexDigit));
        assertTrue(".,+-".chars().noneMatch(Primitives::isHexDigit));
    }
    @Test
    public void testFindInt()
    {
        assertEquals(123456789, Primitives.findInt("  123456789   "));
        assertEquals(123456789, Primitives.findInt("123456789   "));
        assertEquals(123456789, Primitives.findInt("  123456789"));
        assertEquals(123456789, Primitives.findInt("123456789"));
        assertEquals(4567, Primitives.findInt("  123456789   ", 5, 9));
        assertEquals(4567, Primitives.findInt("  1234567", 5, 9));
        assertEquals(0726746425, Primitives.findInt("  726746425   ", 8));
        assertEquals(0x75bcd15, Primitives.findInt("  75bcd15   ", 16));
    }
    @Test
    public void testFindLong()
    {
        assertEquals(123456789L, Primitives.findLong("  123456789   "));
        assertEquals(4567, Primitives.findInt("  123456789   ", 5, 9));
        assertEquals(0726746425L, Primitives.findLong("  726746425   ", 8));
        assertEquals(0x75bcd15L, Primitives.findLong("  75bcd15   ", 16));
    }
    @Test
    public void testFindFloat()
    {
        assertEquals(-12345.6789F, Primitives.findFloat("  -12345.6789   "), 1e-10);
        assertEquals(-12345F, Primitives.findFloat("  -12345.   "), 1e-10);
        assertEquals(-12345F, Primitives.findFloat("  -12345.6789   ", 2, 9), 1e-10);
        assertEquals(.6789F, Primitives.findFloat("  .6789   "), 1e-10);
    }
    @Test
    public void testFindDouble()
    {
        assertEquals(-12345.6789, Primitives.findDouble("  -12345.6789   "), 1e-10);
        assertEquals(-12345, Primitives.findDouble("  -12345.   "), 1e-10);
        assertEquals(.6789, Primitives.findDouble("  .6789   "), 1e-10);
    }
    @Test
    public void testFindScientific()
    {
        assertEquals(-12345.6789e10, Primitives.findScientific("  -12345.6789E10   "), 1e-10);
        assertEquals(-12345e-2, Primitives.findScientific("  -12345.e-2   "), 1e-10);
        assertEquals(.6789e3, Primitives.findScientific("  .6789e3   "), 1e-10);
    }
    @Test
    public void testEquals1()
    {
        assertTrue(Primitives.equals(0, 0));
        assertTrue(Primitives.equals(-12345e-2, 3*-12345e-2/3));
        assertFalse(Primitives.equals(-12345e-2, -12346e-2));
    }
    @Test
    public void testEquals2()
    {
        assertTrue(Primitives.equals(0F, 0F));
        assertTrue(Primitives.equals((float)(-12345e-2F), (float)(3*-12345e-2/3)));
        assertFalse(Primitives.equals((float)(-12345e-2F), (float)(-12346e-2)));
    }
    @Test
    public void testMaxDigits()
    {
        assertEquals(4, Primitives.maxByteDigits(10));
        assertEquals(9, Primitives.maxByteDigits(2));
        assertEquals(17, Primitives.maxShortDigits(2));
        assertEquals(33, Primitives.maxIntDigits(2));
        assertEquals(65, Primitives.maxLongDigits(2));
    }    
}
