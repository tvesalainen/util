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

import static java.lang.Math.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.navi.Tide.PERIOD;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TideTest
{
    
    public TideTest()
    {
    }

    @Test
    public void testToDegrees()
    {
        assertEquals(360, Tide.toDegrees(PERIOD, TimeUnit.MILLISECONDS), 1e-10);
    }
    @Test
    public void testToRadians()
    {
        assertEquals(2*PI, Tide.toRadians(PERIOD, TimeUnit.MILLISECONDS), 1e-10);
    }
    @Test
    public void testTide1()
    {
        Tide tide = new Tide(1, 0, 90, 0);
        assertEquals(1, tide.getTide(PERIOD), 1e-10);
        assertEquals(1, tide.getTide(-PERIOD), 1e-10);
    }
    @Test
    public void testTide2()
    {
        Tide tide = new Tide(1, 0, 90, 0);
        assertEquals(0.5, tide.getTide(0, 60), 1e-10);
        assertEquals(0.5, tide.getTide(0, -60), 1e-10);
    }
    
}
