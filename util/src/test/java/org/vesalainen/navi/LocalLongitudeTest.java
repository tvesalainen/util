/*
 * Copyright (C) 2014 Timo Vesalainen
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

/**
 *
 * @author Timo Vesalainen
 */
public class LocalLongitudeTest
{
    private static final double Epsilon = 1e-8;
    public LocalLongitudeTest()
    {
    }

    @Test
    public void testGetInstance()
    {
        LocalLongitude l = LocalLongitude.getInstance(180, 0);
        assertEquals(-180, l.getInternal(180), Epsilon);
        assertEquals(-180.1, l.getInternal(179.9), Epsilon);
        assertEquals(179.9, l.getExternal(-180.1), Epsilon);
        for (double ii=0.1;ii<=360;ii+=0.1)
        {
            double lon;
            if (ii >= 180)
            {
                lon = -360+ii;
            }
            else
            {
                lon = ii;
            }
            l = LocalLongitude.getInstance(lon, 0);
            double internal = l.getInternal(lon);
            double external = l.getExternal(internal);
            assertEquals(lon, external, Epsilon);
            assertEquals(0.1, internal-l.getInternal(lon-0.1), Epsilon);
            assertEquals(0.1, l.getInternal(lon+0.1)-internal, Epsilon);
            l = LocalLongitude.getInstance(lon, 60);
            internal = l.getInternal(lon);
            external = l.getExternal(internal);
            assertEquals(lon, external, Epsilon);
            assertEquals(0.05, internal-l.getInternal(lon-0.1), Epsilon);
            assertEquals(0.05, l.getInternal(lon+0.1)-internal, Epsilon);
        }
    }

}
