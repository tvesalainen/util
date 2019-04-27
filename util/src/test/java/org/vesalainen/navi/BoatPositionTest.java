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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatPositionTest
{
    
    public BoatPositionTest()
    {
    }

    @Test
    public void testCenterLatitude()
    {
        double heading = 0;
        double lat = 60;
        double lon = 25;
        BoatPosition pt = new BoatPosition(4, 0, 12, 0);
        double expLat = lat + Navis.deltaLatitude(UnitType.convert(6, Meter, NM), heading);
        assertEquals(expLat, pt.centerLatitude(lat, lon, heading), 1e-10);
    }
    @Test
    public void testCenterLongitude()
    {
        double heading = 90;
        double lat = 0;
        double lon = 25;
        BoatPosition pt = new BoatPosition(4, 0, 12, 0);
        double expLon = lon + Navis.deltaLongitude(lat, UnitType.convert(6, Meter, NM), heading);
        assertEquals(expLon, pt.centerLongitude(lat, lon, heading), 1e-10);
    }
    
}
