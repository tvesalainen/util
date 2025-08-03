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
public class VelocityTest
{
    
    public VelocityTest()
    {
    }

    /**
     * Test of asKnots method, of class Velocity.
     */
    @Test
    public void testAsKnots()
    {
    }

    /**
     * Test of getDistance method, of class Velocity.
     */
    @Test
    public void testGetDistance()
    {
    }

    /**
     * Test of getTimeSpan method, of class Velocity.
     */
    @Test
    public void testGetTimeSpan()
    {
    }

    /**
     * Test of getThere method, of class Velocity.
     */
    @Test
    public void testGetThere()
    {
    }

    /**
     * Test of getMetersInSecond method, of class Velocity.
     */
    @Test
    public void testGetMetersInSecond()
    {
    }

    /**
     * Test of toKiloMetersInHour method, of class Velocity.
     */
    @Test
    public void testGetKiloMetersInHour_0args()
    {
    }

    /**
     * Test of toKnots method, of class Velocity.
     */
    @Test
    public void testGetKnots_0args()
    {
    }

    /**
     * Test of toKiloMetersInHour method, of class Velocity.
     */
    @Test
    public void testGetKiloMetersInHour_double()
    {
        assertEquals(1.0, Velocity.toKiloMetersInHour(1000.0/3600.0), Velocity.Epsilon);
    }

    /**
     * Test of toKnots method, of class Velocity.
     */
    @Test
    public void testGetKnots_double()
    {
        assertEquals(1.0, Velocity.toKnots(1852.0/3600.0), Scalar.Epsilon);
    }

    /**
     * Test of toString method, of class Velocity.
     */
    @Test
    public void testToString()
    {
    }
    
}
