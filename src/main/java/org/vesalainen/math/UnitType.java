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
    GRAVITY(UnitCategory.Acceleration, "g"),
    PASCAL(UnitCategory.Pressure, "Pascal"),
    BAR(UnitCategory.Pressure, "Bar"),
    DEGREE(UnitCategory.PlaneAngle, "Degree"),
    RADIAN(UnitCategory.PlaneAngle, "Radian"),
    FAHRENHEIT(UnitCategory.Temperature, "Fahrenheit"),
    CELSIUS(UnitCategory.Temperature, "Celcius"),
    FATHOM(UnitCategory.Length, "Fathom"),
    METER(UnitCategory.Length, "m"),
    KILOMETER(UnitCategory.Length, "Km"),
    NM(UnitCategory.Length, "NM"),
    KNOT(UnitCategory.Speed, "Knots"),
    MS(UnitCategory.Speed, "m/s"),
    KMH(UnitCategory.Speed, "Km/h"),
    DEG(UnitCategory.Coordinate, "˚"),
    DEGMIN(UnitCategory.Coordinate, ""),
    DEGMINSEC(UnitCategory.Coordinate, "")
    ;
    private final UnitCategory category;
    private final String unit;

    private UnitType(UnitCategory category, String unit)
    {
        this.category = category;
        this.unit = unit;
    }

    public UnitCategory getCategory()
    {
        return category;
    }
    
    
}
