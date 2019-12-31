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
package org.vesalainen.navi;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.UnitType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateFormatTest
{
    
    public CoordinateFormatTest()
    {
    }

    @Test
    public void test1()
    {
        assertEquals("N 60.500000°", CoordinateFormat.formatLatitude(Locale.US, 60.5, UnitType.COORDINATE_DEGREES));
        assertEquals("S 60° 30,000'", CoordinateFormat.formatLatitude(Locale.forLanguageTag("FI"), -60.5, UnitType.COORDINATE_DEGREES_AND_MINUTES));
        assertEquals("N 60° 30' 0.0\"", CoordinateFormat.formatLatitude(Locale.US, 60.5, UnitType.COORDINATE_DEGREES_MINUTES_SECONDS));
        assertEquals("W 25.500000°", CoordinateFormat.formatLongitude(Locale.US, -25.5, UnitType.COORDINATE_DEGREES));
        assertEquals("E 25° 30.000'", CoordinateFormat.formatLongitude(Locale.US, 25.5, UnitType.COORDINATE_DEGREES_AND_MINUTES));
        assertEquals("W 25° 30' 0.0\"", CoordinateFormat.formatLongitude(Locale.US, -25.5, UnitType.COORDINATE_DEGREES_MINUTES_SECONDS));
    }
    
}
