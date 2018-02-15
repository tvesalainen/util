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
package org.vesalainen.ham.maidenhead;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MaidenheadLocatorTest
{
    private static final double EPSILON = 0.3;
    public MaidenheadLocatorTest()
    {
    }

    @Test
    public void testFK00CA()
    {
        Location loc = new Location(10.02, -79.79);
        MaidenheadLocator mloc = new MaidenheadLocator(loc);
        assertEquals("FK00CA", mloc.getSubsquare());
        MaidenheadLocator mloc2 = new MaidenheadLocator("FK00CA");
    }
    @Test
    public void testJF96HD()
    {
        Location loc = new Location(-33.85, 18.63);
        MaidenheadLocator mloc = new MaidenheadLocator(loc);
        assertEquals("JF96HD", mloc.getSubsquare());
    }
    @Test
    public void testGetLatitude()
    {
        MaidenheadLocator mloc = new MaidenheadLocator("FK00CA");
        assertEquals(10.02, mloc.getLatitude(), EPSILON);
        
        MaidenheadLocator mloc2 = new MaidenheadLocator("JF96HD");
        assertEquals(-33.85, mloc2.getLatitude(), EPSILON);
    }
    @Test
    public void testGetLongitude()
    {
        MaidenheadLocator mloc = new MaidenheadLocator("JF96HD");
        assertEquals(18.3, mloc.getLongitude(), EPSILON);
        
        MaidenheadLocator mloc2 = new MaidenheadLocator("FK00CA");
        assertEquals(-79.79, mloc2.getLongitude(), EPSILON);
    }
}
