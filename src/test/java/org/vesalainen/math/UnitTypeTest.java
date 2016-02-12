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
package org.vesalainen.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class UnitTypeTest
{
    private static final double Epsilon = 1e-10;
    
    public UnitTypeTest()
    {
    }

    @Test
    public void test0()
    {
        try
        {
            UnitType.FOOT.convert(1, UnitType.KNOT);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    
    @Test
    public void test1()
    {
        assertEquals(1852, UnitType.NM.convert(1, UnitType.METER), Epsilon);
        assertEquals(0.3333333333333, UnitType.FOOT.convert(1, UnitType.YARD), Epsilon);
        assertEquals(12, UnitType.FOOT.convert(1, UnitType.INCH), Epsilon);
        assertEquals(1.6093427125258297, UnitType.MH.convert(1, UnitType.KMH), Epsilon);
    }
    @Test
    public void testFahrenheit()
    {
        assertEquals(32, UnitType.CELSIUS.convert(0, UnitType.FAHRENHEIT), Epsilon);
        assertEquals(0, UnitType.FAHRENHEIT.convert(32, UnitType.CELSIUS), Epsilon);
        assertEquals(30.2, UnitType.CELSIUS.convert(-1, UnitType.FAHRENHEIT), Epsilon);
        assertEquals(-1, UnitType.FAHRENHEIT.convert(30.2, UnitType.CELSIUS), Epsilon);
        assertEquals(-58, UnitType.CELSIUS.convert(-50, UnitType.FAHRENHEIT), Epsilon);
    }
    @Test
    public void testKelvin()
    {
        assertEquals(273.15, UnitType.CELSIUS.convert(0, UnitType.KELVIN), Epsilon);
        assertEquals(0, UnitType.KELVIN.convert(273.15, UnitType.CELSIUS), Epsilon);
    }
    @Test
    public void testDegree()
    {
        assertEquals(2*Math.PI, UnitType.DEGREE.convert(360, UnitType.RADIAN), Epsilon);
        assertEquals(180, UnitType.RADIAN.convert(Math.PI, UnitType.DEGREE), Epsilon);
    }
    @Test
    public void testPressure()
    {
        assertEquals(1e-5, UnitType.PASCAL.convert(1, UnitType.BAR), Epsilon);
        assertEquals(9.8692e-6, UnitType.PASCAL.convert(1, UnitType.ATM), Epsilon);
    }
    @Test
    public void testAcceleration()
    {
        assertEquals(9.80665, UnitType.GFORCEEARTH.convert(1, UnitType.MSS), Epsilon);
    }
}
