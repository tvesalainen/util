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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NavisTest
{
    private static final double Epsilon = 1e-10;
    public NavisTest()
    {
    }

    @Test
    public void testDeltaLatitude1()
    {
        assertEquals(1, Navis.deltaLatitude(60, 0), Epsilon);
        assertEquals(0, Navis.deltaLatitude(60, 270), Epsilon);
        assertEquals(-1, Navis.deltaLatitude(60, 180), Epsilon);
        assertEquals(0.5, Navis.deltaLatitude(60, 60), Epsilon);
    }
    
    @Test
    public void testDeltaLongitude1()
    {
        assertEquals(0, Navis.deltaLongitude(60, 60, 0), Epsilon);
        assertEquals(2, Navis.deltaLongitude(60, 60, 90), Epsilon);
        assertEquals(-2, Navis.deltaLongitude(60, 60, 270), Epsilon);
        assertEquals(-1.0/30.0, Navis.deltaLongitude(60, 1, 270), Epsilon);
        assertEquals(1.0, Navis.deltaLongitude(60, 60, 30), Epsilon);
    }
    
    @Test
    public void testDeltaLongitude2()
    {
        assertEquals(0, Navis.deltaLongitude(0, 60, 0), Epsilon);
        assertEquals(1, Navis.deltaLongitude(0, 60, 90), Epsilon);
        assertEquals(-1, Navis.deltaLongitude(0, 60, 270), Epsilon);
        assertEquals(0.5, Navis.deltaLongitude(0, 60, 30), Epsilon);
    }
    
    @Test
    public void testBearing1()
    {
        WP wp1 = new WP(0, 60, 25);
        WP wp2 = new WP(0, 60, 24);
        assertEquals(270, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(90, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    @Test
    public void testBearing2()
    {
        WP wp1 = new WP(0, -0.5F, 24);
        WP wp2 = new WP(0, 0.5F, 25);
        assertEquals(45, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(225, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    @Test
    public void testBearing3()
    {
        WP wp1 = new WP(0, 59.5F, 24);
        WP wp2 = new WP(0, 60.5F, 26);
        assertEquals(45, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(225, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    @Test
    public void testAddLongitude()
    {
        assertEquals(26, Navis.addLongitude(25, 1), Epsilon);
        assertEquals(-24, Navis.addLongitude(-25, 1), Epsilon);
        assertEquals(-179, Navis.addLongitude(179, 2), Epsilon);
        assertEquals(-1, Navis.addLongitude(1, -2), Epsilon);
    }
    @Test
    public void testDistance()
    {
        assertEquals(120, Navis.distance(0, 179, 0, -179), 0.1);
    }
    @Test
    public void testGHA()
    {
        assertEquals(45, Navis.longitudeToGHA(-45), Epsilon);
        assertEquals(-45, Navis.ghaToLongitude(45), Epsilon);
        assertEquals(359, Navis.longitudeToGHA(1), Epsilon);
        assertEquals(1, Navis.ghaToLongitude(359), Epsilon);
        assertEquals(181, Navis.longitudeToGHA(179), Epsilon);
        assertEquals(179, Navis.ghaToLongitude(181), Epsilon);
    }
    @Test
    public void testNormalizeAngle()
    {
        assertEquals(45, Navis.normalizeAngle(45), Epsilon);
        assertEquals(10, Navis.normalizeAngle(370), Epsilon);
        assertEquals(350, Navis.normalizeAngle(-10), Epsilon);
        assertEquals(350, Navis.normalizeAngle(-370), Epsilon);
    }
    
    @Test
    public void testNormalizeToHalfAngle()
    {
        assertEquals(45, Navis.normalizeToHalfAngle(45), Epsilon);
        assertEquals(180, Navis.normalizeToHalfAngle(180), Epsilon);
        assertEquals(179, Navis.normalizeToHalfAngle(181), Epsilon);
        assertEquals(10, Navis.normalizeToHalfAngle(350), Epsilon);
    }
    @Test
    public void testSigned()
    {
        assertEquals(-20, Navis.signed(340), Epsilon);
        assertEquals(20, Navis.signed(20), Epsilon);
    }
    @Test
    public void testAngleDiff()
    {
        assertEquals(-20, Navis.angleDiff(340, 320), Epsilon);
        assertEquals(20, Navis.angleDiff(320, 340), Epsilon);
        assertEquals(20, Navis.angleDiff(350, 10), Epsilon);
    }
    @Test
    public void testFathom()
    {
        assertEquals(1.8288, Navis.fathomsToMeters(1), Epsilon);
        assertEquals(1, Navis.metersToFathoms(1.8288), Epsilon);
    }
    
    @Test
    public void testFeets()
    {
        assertEquals(0.3048, Navis.feetsToMeters(1), Epsilon);
        assertEquals(1, Navis.metersToFeets(0.3048), Epsilon);
    }
    
    @Test
    public void testKnots()
    {
        double v = 1852.0/3600.0;
        assertEquals(v, Navis.knotsToMetersPerSecond(1.0), Epsilon);
        assertEquals(1, Navis.metersPerSecondToKnots(v), Epsilon);
    }

    @Test
    public void testKMH()
    {
        double e = 1000.0/3600.0;
        assertEquals(e, Navis.kiloMetersInHourToMetersPerSecond(1.0), Epsilon);
        assertEquals(1, Navis.metersPerSecondToKiloMetersInHour(e), Epsilon);
    }

    private class WP implements WayPoint
    {
        private long time;
        private float latitude;
        private float longitude;

        public WP(long time, float latitude, float longitude)
        {
            this.time = time;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public long getTime()
        {
            return time;
        }

        @Override
        public double getLatitude()
        {
            return latitude;
        }

        @Override
        public double getLongitude()
        {
            return longitude;
        }

        @Override
        public String toString()
        {
            return "WayPoint{" + "time=" + time + ", latitude=" + latitude + ", longitude=" + longitude + '}';
        }
        
    }
}
