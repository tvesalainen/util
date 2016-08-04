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
    public void testErr()
    {
        try
        {
            UnitType.Foot.convertTo(1, UnitType.Knot);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    
    @Test
    public void testLength()
    {
        assertEquals(1852, UnitType.NM.convertTo(1, UnitType.Meter), Epsilon);
        assertEquals(0.3333333333333, UnitType.Foot.convertTo(1, UnitType.Yard), Epsilon);
        assertEquals(12, UnitType.Foot.convertTo(1, UnitType.Inch), Epsilon);
        assertEquals(1.6093427125258297, UnitType.MH.convertTo(1, UnitType.KMH), Epsilon);
    }
    @Test
    public void testFahrenheit()
    {
        assertEquals(32, UnitType.Celsius.convertTo(0, UnitType.Fahrenheit), Epsilon);
        assertEquals(0, UnitType.Fahrenheit.convertTo(32, UnitType.Celsius), Epsilon);
        assertEquals(30.2, UnitType.Celsius.convertTo(-1, UnitType.Fahrenheit), Epsilon);
        assertEquals(-1, UnitType.Fahrenheit.convertTo(30.2, UnitType.Celsius), Epsilon);
        assertEquals(-58, UnitType.Celsius.convertTo(-50, UnitType.Fahrenheit), Epsilon);
    }
    @Test
    public void testKelvin()
    {
        assertEquals(273.15, UnitType.Celsius.convertTo(0, UnitType.Kelvin), Epsilon);
        assertEquals(0, UnitType.Kelvin.convertTo(273.15, UnitType.Celsius), Epsilon);
    }
    @Test
    public void testDegree()
    {
        assertEquals(2*Math.PI, UnitType.Degree.convertTo(360, UnitType.Radian), Epsilon);
        assertEquals(180, UnitType.Radian.convertTo(Math.PI, UnitType.Degree), Epsilon);
    }
    @Test
    public void testPressure()
    {
        assertEquals(1e-5, UnitType.Pascal.convertTo(1, UnitType.BAR), Epsilon);
        assertEquals(9.8692e-6, UnitType.Pascal.convertTo(1, UnitType.ATM), Epsilon);
    }
    @Test
    public void testAcceleration()
    {
        assertEquals(9.80665, UnitType.GForceEarth.convertTo(1, UnitType.MSS), Epsilon);
    }
    @Test
    public void testBeaufort()
    {
        assertEquals(0.837, UnitType.Beaufort.convertTo(1, UnitType.MS), Epsilon);
        assertEquals(1, UnitType.MS.convertTo(0.837, UnitType.Beaufort), Epsilon);
        assertEquals(2.367393503412561, UnitType.Beaufort.convertTo(2, UnitType.MS), Epsilon);
        assertEquals(2, UnitType.MS.convertTo(2.367393503412561, UnitType.Beaufort), Epsilon);
        assertEquals(26.468264015609332, UnitType.Beaufort.convertTo(10, UnitType.MS), Epsilon);
        assertEquals(10, UnitType.MS.convertTo(26.468264015609332, UnitType.Beaufort), Epsilon);
    }
    @Test
    public void testStatic()
    {
        assertEquals(9.80665, UnitType.convert(1, UnitType.GForceEarth, UnitType.MSS), Epsilon);
    }
    @Test
    public void testDegreeNeg()
    {
        assertEquals(350, UnitType.DegreeNeg.convertTo(-10, UnitType.Degree), Epsilon);
        assertEquals(160, UnitType.DegreeNeg.convertTo(160, UnitType.Degree), Epsilon);
        assertEquals(-90, UnitType.Degree.convertTo(270, UnitType.DegreeNeg), Epsilon);
    }
}
