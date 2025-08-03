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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationCenterTest
{
    
    public LocationCenterTest()
    {
    }

    @Test
    public void test1()
    {
        LocationCenter c = new LocationCenter();
        c.add(25, 60);
        c.add(25, 61);
        c.add(24, 60);
        c.add(24, 61);
        assertEquals(24.5, c.longitude(), 1e-10);
        assertEquals(60.5, c.latitude(), 1e-10);
    }
    @Test
    public void test2()
    {
        LocationCenter c = new LocationCenter();
        c.add(175, 30);
        c.add(-175, 30);
        assertEquals(180, c.longitude(), 1e-10);
    }
    
}
