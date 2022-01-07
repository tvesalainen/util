/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import static java.lang.Math.PI;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
            UnitType.FOOT.convertTo(1, UnitType.KNOT);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    
    @Test
    public void testLength()
    {
        assertEquals(1852, UnitType.NAUTICAL_MILE.convertTo(1, UnitType.METER), Epsilon);
        assertEquals(0.3333333333333, UnitType.FOOT.convertTo(1, UnitType.YARD), Epsilon);
        assertEquals(12, UnitType.FOOT.convertTo(1, UnitType.INCH), Epsilon);
        assertEquals(1.6093427125258297, UnitType.MILES_PER_HOUR.convertTo(1, UnitType.KILO_METERS_PER_HOUR), Epsilon);
    }
    @Test
    public void testFahrenheit()
    {
        assertEquals(32, UnitType.CELSIUS.convertTo(0, UnitType.FAHRENHEIT), Epsilon);
        assertEquals(0, UnitType.FAHRENHEIT.convertTo(32, UnitType.CELSIUS), Epsilon);
        assertEquals(30.2, UnitType.CELSIUS.convertTo(-1, UnitType.FAHRENHEIT), Epsilon);
        assertEquals(-1, UnitType.FAHRENHEIT.convertTo(30.2, UnitType.CELSIUS), Epsilon);
        assertEquals(-58, UnitType.CELSIUS.convertTo(-50, UnitType.FAHRENHEIT), Epsilon);
    }
    @Test
    public void testKelvin()
    {
        assertEquals(273.15, UnitType.CELSIUS.convertTo(0, UnitType.KELVIN), Epsilon);
        assertEquals(0, UnitType.KELVIN.convertTo(273.15, UnitType.CELSIUS), Epsilon);
    }
    @Test
    public void testDegree()
    {
        assertEquals(2*Math.PI, UnitType.DEGREE.convertTo(360, UnitType.RADIAN), Epsilon);
        assertEquals(180, UnitType.RADIAN.convertTo(Math.PI, UnitType.DEGREE), Epsilon);
    }
    @Test
    public void testPressure()
    {
        assertEquals(1e-5, UnitType.PASCAL.convertTo(1, UnitType.BAR), Epsilon);
        assertEquals(9.8692e-6, UnitType.PASCAL.convertTo(1, UnitType.ATM), Epsilon);
    }
    @Test
    public void testAcceleration()
    {
        assertEquals(9.80665, UnitType.GFORCE_EARTH.convertTo(1, UnitType.MSS), Epsilon);
    }
    @Test
    public void testBeaufort()
    {
        assertEquals(0.837, UnitType.BEAUFORT.convertTo(1, UnitType.METERS_PER_SECOND), Epsilon);
        assertEquals(1, UnitType.METERS_PER_SECOND.convertTo(0.837, UnitType.BEAUFORT), Epsilon);
        assertEquals(2.367393503412561, UnitType.BEAUFORT.convertTo(2, UnitType.METERS_PER_SECOND), Epsilon);
        assertEquals(2, UnitType.METERS_PER_SECOND.convertTo(2.367393503412561, UnitType.BEAUFORT), Epsilon);
        assertEquals(26.468264015609332, UnitType.BEAUFORT.convertTo(10, UnitType.METERS_PER_SECOND), Epsilon);
        assertEquals(10, UnitType.METERS_PER_SECOND.convertTo(26.468264015609332, UnitType.BEAUFORT), Epsilon);
    }
    @Test
    public void testStatic()
    {
        assertEquals(9.80665, UnitType.convert(1, UnitType.GFORCE_EARTH, UnitType.MSS), Epsilon);
    }
    @Test
    public void testDegreeNeg()
    {
        assertEquals(350, UnitType.DEGREE_NEG.convertTo(-10, UnitType.DEGREE), Epsilon);
        assertEquals(160, UnitType.DEGREE_NEG.convertTo(160, UnitType.DEGREE), Epsilon);
        assertEquals(-90, UnitType.DEGREE.convertTo(270, UnitType.DEGREE_NEG), Epsilon);
    }
    @Test
    public void testSpeed()
    {
        assertEquals(1.852, UnitType.KNOT.convertTo(1, UnitType.KILO_METERS_PER_HOUR), 1e-3);
        assertEquals(3.3, UnitType.KILO_METERS_PER_HOUR.convertTo(6.1, UnitType.KNOT), 1e-1);
    }
    @Test
    public void testRateOfTurn()
    {
        assertEquals(60, UnitType.DEGREES_PER_SECOND.convertTo(1, UnitType.DEGREES_PER_MINUTE), 1e-3);
        assertEquals(1, UnitType.DEGREES_PER_MINUTE.convertTo(60, UnitType.DEGREES_PER_SECOND), 1e-3);
        assertEquals(1, UnitType.DEGREES_PER_SECOND.convertTo(180, UnitType.RADIANS_PER_SECOND), 1e-3);
        assertEquals(180, UnitType.RADIANS_PER_SECOND.convertTo(1, UnitType.DEGREES_PER_SECOND), 1e-3);
        assertEquals(0.33749999999999997, RADIANS_PER_SECOND.convertTo(3.125e-05, DEGREES_PER_MINUTE), 1e-3);
        assertEquals(0.108, RADIANS_PER_SECOND.convertTo(0.00001, DEGREES_PER_MINUTE), 1e-3);
    }
    @Test
    public void testDuration()
    {
        assertEquals(60, UnitType.DURATION_MINUTES.convertTo(1, UnitType.DURATION_SECONDS), 1e-3);
        assertEquals(60, UnitType.DURATION_HOURS.convertTo(1, UnitType.DURATION_MINUTES), 1e-3);
        assertEquals(24, UnitType.DURATION_DAYS.convertTo(1, UnitType.DURATION_HOURS), 1e-3);
    }
    @Test
    public void testParse()
    {
        assertEquals(60, DURATION_SECONDS.parse("1m"), 1e-10);
        assertEquals(60, DURATION_MINUTES.parse("1h"), 1e-10);
    }    
}
