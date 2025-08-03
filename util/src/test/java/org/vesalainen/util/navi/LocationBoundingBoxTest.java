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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.test.Asserts;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationBoundingBoxTest
{
    
    public LocationBoundingBoxTest()
    {
    }

    @Test
    public void test()
    {
                                                        //n   e    s   w
        LocationBoundingBox bb1 = new LocationBoundingBox(30, 170, 20, 160);
        assertEquals(30, bb1.getNorth(), 1e-10);
        assertEquals(170, bb1.getEast(), 1e-10);
        assertEquals(20, bb1.getSouth(), 1e-10);
        assertEquals(160, bb1.getWest(), 1e-10);
        Location southWest = bb1.getSouthWest();
        assertEquals(20, southWest.getLatitude(), 1e-10);
        assertEquals(160, southWest.getLongitude(), 1e-10);
        Location northEast = bb1.getNorthEast();
        assertEquals(30, northEast.getLatitude(), 1e-10);
        assertEquals(170, northEast.getLongitude(), 1e-10);
        Asserts.assertSerializable(bb1);
    }
    
}
