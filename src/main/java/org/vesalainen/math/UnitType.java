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

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author tkv
 */
public enum UnitType
{
    /**
     * m/s<sup>2</sup>
     */
    MSS(UnitCategory.Acceleration, 1.0, "g"),
    /**
     * <i>g</i>0 Standard acceleration
     */
    GForceEarth(UnitCategory.Acceleration, 9.80665, "g"),
    /**
     * Pascal
     */
    Pascal(UnitCategory.Pressure, 1.0, "Pa"),
    /**
     * hPa 
     */
    HPA(UnitCategory.Pressure, 100.0, "hPa"),
    /**
     * Bar
     */
    BAR(UnitCategory.Pressure, 100000.0, "bar"),
    /**
     * Atmosphere
     */
    ATM(UnitCategory.Pressure, 101325.0, "atm"),
    /**
     * Degree 0 - 360
     */
    Degree(UnitCategory.PlaneAngle, 1.0, "\u00B0"),
    /**
     * Degree -180 - 180
     */
    DegreeNeg(UnitCategory.PlaneAngle, "\u00B0", (double v)->{return v > 180 ? v-360 : v;}, (double v)->{return v < 0 ? 360+v : v;}),
    /**
     * Radians
     */
    Radian(UnitCategory.PlaneAngle, Math.toDegrees(1), "Rad"),
    /**
     * Celsius
     */
    Celsius(UnitCategory.Temperature, 1.0, "\u00B0C"),
    /**
     * Fahrenheit
     */
    Fahrenheit(UnitCategory.Temperature, "Fahrenheit", (double v)->{return v*1.8+32.0;}, (double v)->{return (v-32.0)/1.8;}),
    /**
     * Kelvin
     */
    Kelvin(UnitCategory.Temperature, "Kelvin", (double v)->{return v+273.15;}, (double v)->{return v-273.15;}),
    /**
     * Fathom
     */
    Fathom(UnitCategory.Length, 1.8288, "Fathom"),
    /**
     * Meter
     */
    Meter(UnitCategory.Length, 1.0, "m"),
    /**
     * Mile
     */
    Mile(UnitCategory.Length, 1609.34, "Mile"),
    /**
     * Foot
     */
    Foot(UnitCategory.Length, 0.3048, "Foot"),
    /**
     * Yard
     */
    Yard(UnitCategory.Length, 0.9144, "Yard"),
    /**
     * Inch
     */
    Inch(UnitCategory.Length, 0.0254, "Inch"),
    /**
     * Kilometer
     */
    KiloMeter(UnitCategory.Length, 1000.0, "Km"),
    /**
     * Nautical mile
     */
    NM(UnitCategory.Length, 1852.0, "NM"),
    /**
     * Miles/hour
     */
    MH(UnitCategory.Speed, 0.44704, "Miles/h"),
    /**
     * Knot
     */
    Knot(UnitCategory.Speed, 0.514444, "Knots"),
    /**
     * m/s
     */
    MS(UnitCategory.Speed, 1.0, "m/s"),
    /**
     * Km/h
     */
    KMH(UnitCategory.Speed, 0.277778, "Km/h"),
    /**
     * Beaufort
     */
    Beaufort(UnitCategory.Speed, "B", (double v)->{return Math.round(Math.pow(v/0.837, 2.0/3.0));}, (double v)->{return 0.837*Math.pow(v, 3.0/2.0);}),
    /**
     * Coordinate degrees
     */
    Deg(UnitCategory.Coordinate, 1, "˚"),
    /**
     * Coordinate degrees and minutes
     */
    DegMin(UnitCategory.Coordinate, 1, ""),
    /**
     * Coordinate degrees, minutes and seconds
     */
    DegMinSec(UnitCategory.Coordinate, 1, ""),
    /**
     * Unitless
     */
    Unitless(UnitCategory.Unknown, 1, "")
    ;
    private final UnitCategory category;
    private final String unit;
    private DoubleUnaryOperator fromSI;
    private DoubleUnaryOperator toSI;

    private UnitType(UnitCategory category, double multiplier, String unit)
    {
        this(category, unit, (double v)->{ return v/multiplier;}, (double v)->{ return v*multiplier;});
    }

    private UnitType(UnitCategory category, double multiplier, double offset, String unit)
    {
        this(category, unit, (double v)->{ return v/multiplier-offset;}, (double v)->{ return v*multiplier+offset;});
    }

    private UnitType(UnitCategory category, String unit, DoubleUnaryOperator fromSI, DoubleUnaryOperator toSI)
    {
        this.category = category;
        this.unit = unit;
        this.fromSI = fromSI;
        this.toSI = toSI;
    }

    public double convertTo(double value, UnitType to)
    {
        if (equals(to))
        {
            return value;
        }
        check(this, to);
        return to.fromSI.applyAsDouble(toSI.applyAsDouble(value));
    }
    
    public static double convert(double value, UnitType from, UnitType to)
    {
        return from.convertTo(value, to);
    }
    
    private static void check(UnitType from, UnitType to)
    {
        if (!from.category.equals(to.getCategory()))
        {
            throw new IllegalArgumentException(from+" cannot be converted to "+to);
        }
    }
    
    public UnitCategory getCategory()
    {
        return category;
    }
    
    public String getUnit()
    {
        return unit;
    }
}
