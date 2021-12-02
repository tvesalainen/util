/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.math.UnitType.METER;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateMapTest
{
    
    public CoordinateMapTest()
    {
    }

    @Test
    public void test1()
    {
        CoordinateMap<String> m = new CoordinateMap<>(60, 1, METER);
        m.put(25, 60, "25,60");
        m.put(24, 60, "24,60");
        assertEquals("25,60", m.get(25, 60));
        m.forEach((lon,lat, s)->System.err.println(lon+", "+lat+", "+s));
    }
    
}
