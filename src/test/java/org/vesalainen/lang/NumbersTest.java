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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class NumbersTest
{
    
    public NumbersTest()
    {
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
        assertEquals(0F, Numbers.parseFloat("0"), Math.ulp(0F));
        assertEquals(123456.789F, Numbers.parseFloat("123456.789"), Math.ulp(123456.789F));
        assertEquals(123456.789F, Numbers.parseFloat("123456789E-3"), Math.ulp(123456.789F));
    }

    /**
     * Test of parseDouble method, of class Numbers.
     */
    @Test
    public void testParseDouble()
    {
        assertEquals(0, Numbers.parseDouble("0"), Math.ulp(0));
        assertEquals(123456.789, Numbers.parseDouble("123456.789"), Math.ulp(123456.789));
        assertEquals(123456.789, Numbers.parseDouble("123456789E-3"), Math.ulp(123456.789));
        double max = Double.MAX_VALUE;
        assertEquals(max, Numbers.parseDouble(Double.toString(max)), Math.ulp(max));
        double min = Double.MIN_VALUE;
        assertEquals(min, Numbers.parseDouble(Double.toString(min)), Math.ulp(min));
    }
    
}
