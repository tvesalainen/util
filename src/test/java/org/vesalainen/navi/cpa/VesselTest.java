/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.navi.cpa;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class VesselTest
{
    private static final double Epsilon = 1e-10;
    public VesselTest()
    {
    }

    @Test
    public void test1()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 6, 90, 6);
        double expRad = 3/Math.PI;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60-(expRad/60), v.getCenterLatitude(), Epsilon);
        assertEquals(25, v.getCenterLongitude(), Epsilon);
    }
    
    @Test
    public void test2()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 2*Math.PI, 180, 6);
        double expRad = 1.0;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60, v.getCenterLatitude(), Epsilon);
        assertEquals(25-(expRad/30.0), v.getCenterLongitude(), Epsilon);
    }
    
}
