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
package org.vesalainen.ham.itshfbc;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationFormatterTest
{
    
    public LocationFormatterTest()
    {
    }

    @Test
    public void testFormat1()
    {
        String got = LocationFormatter.format(new Location(60.1, 25.2));
        assertEquals("60.1N25.2E", got);
    }
    @Test
    public void testFormat2()
    {
        String got = LocationFormatter.format(new Location(-60.1, -25.2));
        assertEquals("60.1S25.2W", got);
    }
    @Test
    public void testFormat3()
    {
        String got = LocationFormatter.format(new Location(-60, -25));
        assertEquals("60S25W", got);
    }
    @Test
    public void testParse1()
    {
        Location loc = LocationFormatter.parse("60.1N25.2E");
        assertEquals(60.1, loc.getLatitude(), 1e-10);
        assertEquals(25.2, loc.getLongitude(), 1e-10);
    }    
    @Test
    public void testParse2()
    {
        Location loc = LocationFormatter.parse("60.1S25.2W");
        assertEquals(-60.1, loc.getLatitude(), 1e-10);
        assertEquals(-25.2, loc.getLongitude(), 1e-10);
    }    
}
