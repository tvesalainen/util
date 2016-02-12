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
    GFORCEEARTH(UnitCategory.Acceleration, 9.80665, "g"),
    /**
     * Pascal
     */
    PASCAL(UnitCategory.Pressure, 1.0, "Pa"),
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
     * Degree
     */
    DEGREE(UnitCategory.PlaneAngle, 1.0, "\u00B0"),
    /**
     * Radians
     */
    RADIAN(UnitCategory.PlaneAngle, Math.toDegrees(1), "Radian"),
    /**
     * Celsius
     */
    CELSIUS(UnitCategory.Temperature, 1.0, "\u00B0C"),
    /**
     * Fahrenheit
     */
    FAHRENHEIT(UnitCategory.Temperature, 0.5555555555555556, -32, "Fahrenheit"),
    /**
     * Kelvin
     */
    KELVIN(UnitCategory.Temperature, 1.0, -273.15, "Kelvin"),
    /**
     * Fathom
     */
    FATHOM(UnitCategory.Length, 1.8288, "Fathom"),
    /**
     * Meter
     */
    METER(UnitCategory.Length, 1.0, "m"),
    /**
     * Mile
     */
    MILE(UnitCategory.Length, 1609.34, "Mile"),
    /**
     * Foot
     */
    FOOT(UnitCategory.Length, 0.3048, "Foot"),
    /**
     * Yard
     */
    YARD(UnitCategory.Length, 0.9144, "Yard"),
    /**
     * Inch
     */
    INCH(UnitCategory.Length, 0.0254, "Inch"),
    /**
     * Kilometer
     */
    KILOMETER(UnitCategory.Length, 1000.0, "Km"),
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
    KNOT(UnitCategory.Speed, 0.514444, "Knots"),
    /**
     * m/s
     */
    MS(UnitCategory.Speed, 1.0, "m/s"),
    /**
     * Km/h
     */
    KMH(UnitCategory.Speed, 0.277778, "Km/h"),
    /**
     * Coordinate degrees
     */
    DEG(UnitCategory.Coordinate, Double.NaN, "˚"),
    /**
     * Coordinate degrees and minutes
     */
    DEGMIN(UnitCategory.Coordinate, Double.NaN, ""),
    /**
     * Coordinate degrees, minutes and seconds
     */
    DEGMINSEC(UnitCategory.Coordinate, Double.NaN, "")
    ;
    private final UnitCategory category;
    private final double multiplier;
    private final double offset;
    private final String unit;

    private UnitType(UnitCategory category, double multiplier, double offset, String unit)
    {
        this.category = category;
        this.multiplier = multiplier;
        this.offset = offset;
        this.unit = unit;
    }

    private UnitType(UnitCategory category, double multiplier, String unit)
    {
        this.category = category;
        this.multiplier = multiplier;
        this.offset = 0;
        this.unit = unit;
    }

    public double convert(double value, UnitType to)
    {
        if (!category.equals(to.getCategory()))
        {
            throw new IllegalArgumentException(this+" cannot be converted to "+to);
        }
        if (Double.isNaN(multiplier) || Double.isNaN(to.multiplier))
        {
            throw new UnsupportedOperationException("conversion from "+this+" to "+to+" not supported");
        }
        return (value+offset)*multiplier/to.multiplier-to.offset;
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
