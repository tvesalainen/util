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

package org.vesalainen.util.navi;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class KilometersInHourTest
{
    
    public KilometersInHourTest()
    {
    }

    /**
     * Test of toMetersPerSecond method, of class KilometersInHour.
     */
    @Test
    public void testToMetersPerSecond()
    {
        assertEquals(1000.0/3600.0, KilometersInHour.toMetersPerSecond(1.0), Velocity.Epsilon);
    }

    /**
     * Test of toString method, of class KilometersInHour.
     */
    @Test
    public void testToString()
    {
    }
    
}
