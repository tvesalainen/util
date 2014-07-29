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

    @Test
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
     * Test of Numbers.parseInt method, of class Numbers.
     */
    @Test
    public void testParseInt()
    {
        assertEquals(0, Numbers.parseInt("0", 10));
        assertEquals(473, Numbers.parseInt("473", 10));
        assertEquals(42, Numbers.parseInt("+42", 10));
        assertEquals(0, Numbers.parseInt("-0", 10));
        assertEquals(-255, Numbers.parseInt("-FF", 16));
        assertEquals(102, Numbers.parseInt("1100110", 2));
        assertEquals(2147483647, Numbers.parseInt("2147483647", 10));
        assertEquals(-2147483648, Numbers.parseInt("-2147483648", 10));
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
    
}
