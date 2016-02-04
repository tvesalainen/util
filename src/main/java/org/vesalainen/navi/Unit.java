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
package org.vesalainen.navi;

/**
 *
 * @author tkv
 */
public enum Unit
{
    DEGREE(UnitCategory.Angle),
    FAHRENHEIT(UnitCategory.Temperature),
    CELSIUS(UnitCategory.Temperature),
    FATHOM(UnitCategory.Distance),
    METER(UnitCategory.Distance),
    KILOMETER(UnitCategory.Distance),
    NM(UnitCategory.Distance),
    KNOT(UnitCategory.Speed),
    MS(UnitCategory.Speed),
    KMH(UnitCategory.Speed),
    DEG(UnitCategory.Coordinate),
    DEGMIN(UnitCategory.Coordinate),
    DEGMINSEC(UnitCategory.Coordinate)
    ;
    private final UnitCategory category;

    private Unit(UnitCategory category)
    {
        this.category = category;
    }

    public UnitCategory getCategory()
    {
        return category;
    }
    
    
}
