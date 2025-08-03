/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.navi;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.test.Asserts;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationMapTest
{
    
    public LocationMapTest()
    {
    }

    @Test
    public void test1()
    {
        LocationMap<Location> map = makeMap();
        Asserts.assertSerializable(map);
        LocationBoundingBox bb = new LocationBoundingBox("59,24,60,26");
        List<Location> list = map.strickValues(bb).collect(Collectors.toList());
        assertEquals(6, list.size());
    }
    @Test
    public void test2()
    {
        LocationMap<Location> map = makeMap();
        LocationBoundingBox bb = new LocationBoundingBox("-20,179,-10,-179");
        List<Location> list = map.strickValues(bb).collect(Collectors.toList());
        assertEquals(33, list.size());
    }
    @Test
    public void test3()
    {
        LocationMap<Location> map = makeMap();
        Location loc = new Location(-20, 179);
        map.put(loc, loc);
        loc = new Location(-10, -179);
        map.put(loc, loc);
        LocationBoundingBox bb = new LocationBoundingBox("-20,179,-10,-179");
        List<Location> list = map.strickValues(bb).collect(Collectors.toList());
        assertEquals(35, list.size());
    }
    LocationMap<Location> makeMap()
    {
        LocationMap<Location> map = new LocationMap<>();
        for (double lat=-90;lat<=90;lat++)
        {
            for (double lon=-180;lon<180;lon++)
            {
                Location loc = new Location(lat, lon);
                map.put(loc, loc);
            }
        }
        return map;
    }
}
