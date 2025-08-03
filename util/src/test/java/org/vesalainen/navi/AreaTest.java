/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AreaTest
{
    
    public AreaTest()
    {
    }

    @Test
    public void testConvexPacific()
    {
        Location sw = new Location(-10, -170);
        Location se = new Location(-10, 170);
        Location nw = new Location(10, -170);
        Location ne = new Location(10, 170);
        Area convex = Area.getArea(sw, se, nw, ne);
        assertTrue(convex.isInside(0, 180));
        assertFalse(convex.isInside(0, 160));
        Area convex2 = Area.getArea(convex.getLocations());
        assertTrue(convex2.isInside(0, 180));
        assertFalse(convex2.isInside(0, 160));
    }
    @Test
    public void testConvexEurope()
    {
        Area convex = Area.getSquare(30, 40, -10, 10);
        assertTrue(convex.isInside(35, 5));
        assertFalse(convex.isInside(35, 12));
        Area convex2 = Area.getArea(convex.getLocations());
        assertTrue(convex2.isInside(35, 5));
        assertFalse(convex2.isInside(35, 12));
    }
    @Test
    public void testPolar()
    {
        Area polar = Area.getPolar(30, 40);
        assertTrue(polar.isInside(35, 0));
        assertTrue(polar.isInside(35, 180));
        Area polar2 = Area.getArea(polar.getLocations());
        assertTrue(polar2.isInside(35, 0));
        assertTrue(polar2.isInside(35, 180));
    }
    @Test
    public void testPolar2()
    {
        Area polar = Area.getPolar(30, 40, 0, 90, 180);
        assertTrue(polar.isInside(35, 90));
        assertFalse(polar.isInside(35, 270));
        Area polar2 = Area.getArea(polar.getLocations());
        assertTrue(polar2.isInside(35, 90));
        assertFalse(polar2.isInside(35, 270));
    }

}
