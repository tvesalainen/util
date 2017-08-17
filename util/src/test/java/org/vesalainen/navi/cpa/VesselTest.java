/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VesselTest
{
    private static final double Epsilon = 1e-10;
    public VesselTest()
    {
    }

    @Test
    public void test90_0_1()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 8, 90, 0);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60, v.estimatedLatitude(q), Epsilon);
        assertEquals(25+2.0/30.0, v.estimatedLongitude(q), Epsilon);
    }
    
    @Test
    public void test90_1()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 2*Math.PI, 90, 6);
        double expRad = 1.0;
        double d = expRad/60;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60-d, v.getCenterLatitude(), Epsilon);
        assertEquals(25, v.getCenterLongitude(), Epsilon);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60-d, v.estimatedLatitude(q), Epsilon);
        assertEquals(25+d*2, v.estimatedLongitude(q), Epsilon);
    }
    
    @Test
    public void test90_2()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 2*Math.PI, 90, -6);
        double expRad = 1.0;
        double d = expRad/60;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60+d, v.getCenterLatitude(), Epsilon);
        assertEquals(25, v.getCenterLongitude(), Epsilon);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60+d, v.estimatedLatitude(q), Epsilon);
        assertEquals(25+d*2, v.estimatedLongitude(q), Epsilon);
    }
    
    @Test
    public void test180_0_1()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 8, 180, 0);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60-2.0/60.0, v.estimatedLatitude(q), Epsilon);
        assertEquals(25, v.estimatedLongitude(q), Epsilon);
    }
    
    @Test
    public void test180_1()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 2*Math.PI, 180, 6);
        double expRad = 1.0;
        double d = expRad/60;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60, v.getCenterLatitude(), Epsilon);
        assertEquals(25-d*2, v.getCenterLongitude(), Epsilon);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60-d, v.estimatedLatitude(q), Epsilon);
        assertEquals(25-d*2, v.estimatedLongitude(q), Epsilon);
    }
    
    @Test
    public void test180_2()
    {
        Vessel v = new Vessel();
        v.update(0, 60, 25, 2*Math.PI, 180, -6);
        double expRad = 1.0;
        double d = expRad/60;
        assertEquals(expRad, v.getRadius(), Epsilon);
        assertEquals(60, v.getCenterLatitude(), Epsilon);
        assertEquals(25+d*2, v.getCenterLongitude(), Epsilon);
        assertEquals(60, v.estimatedLatitude(0), Epsilon);
        assertEquals(25, v.estimatedLongitude(0), Epsilon);
        long q = 15*60000;
        assertEquals(60-d, v.estimatedLatitude(q), Epsilon);
        assertEquals(25+d*2, v.estimatedLongitude(q), Epsilon);
    }
    
}
