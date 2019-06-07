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

import java.util.concurrent.TimeUnit;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import org.vesalainen.util.function.DoubleBiConsumer;
import org.vesalainen.util.navi.AbstractLocationSupport.LocationFactory;

/**
 * Collection of navigational methods etc.
 * 
 * <p>coordinates are in degrees. Positive values are north and east.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Navis
{
    @Deprecated public static final double Kilo = 1000;
    @Deprecated public static final double NMInMeters = 1852;
    @Deprecated public static final double FeetInMeters = 0.3048;
    @Deprecated public static final double FathomInMeters = 1.8288;
    @Deprecated public static final double HoursInSeconds = TimeUnit.HOURS.toSeconds(1);
    @Deprecated public static final double NMInMetersPerHoursInSecond = NMInMeters / HoursInSeconds;
    /**
     * Creates center location from coordinates
     * @param <T>
     * @param factory
     * @param coordinates lat1, lon1, ...
     * @return 
     */
    public static <T> T locationCenter(LocationFactory<T> factory, double... coordinates)
    {
        if (coordinates.length % 2 != 0)
        {
            throw new IllegalArgumentException("odd numer of coordinates");
        }
        return locationCenter(factory, (i)->coordinates[2*i], (i)->coordinates[2*i+1], coordinates.length/2);
    }
    /**
     * Creates center location from locations.
     * @param <T>
     * @param factory
     * @param latSup
     * @param lonSup
     * @param count
     * @return 
     */
    public static <T> T locationCenter(LocationFactory<T> factory, IntToDoubleFunction latSup, IntToDoubleFunction lonSup, int count)
    {
        double lat = 0;
        double sin = 0;
        double cos = 0;
        for (int ii=0;ii<count;ii++)
        {
            lat += latSup.applyAsDouble(ii);
            double rad = Math.toRadians(lonSup.applyAsDouble(ii));
            sin += Math.sin(rad);
            cos += Math.cos(rad);
        }
        double atan2 = Math.atan2(sin, cos);
        double lon = Navis.normalizeToHalfAngle(Math.toDegrees(atan2));
        return factory.create(lat/count, lon);
    }
    /**
     * Return latitude change after moving distance at bearing
     * @param distance  NM
     * @param bearing   Degrees
     * @return 
     */
    public static final double deltaLatitude(double distance, double bearing)
    {
        return (Math.cos(Math.toRadians(bearing))*distance)/60;
    }
    /**
     * Return longitude change after moving distance at bearing
     * @param latitude
     * @param distance
     * @param bearing
     * @return 
     */
    public static final double deltaLongitude(double latitude, double distance, double bearing)
    {
        double departure = departure(latitude);
        double sin = Math.sin(Math.toRadians(bearing));
        return (sin*distance)/(60*departure);
    }
    /**
     * Return average departure of two waypoints
     * @param loc1
     * @param loc2
     * @return 
     */
    public static final double departure(WayPoint loc1, WayPoint loc2)
    {
        return departure(loc2.getLatitude(), loc1.getLatitude());
    }
    /**
     * Returns departure of average latitude
     * @param latitude1
     * @param latitude2
     * @return 
     */
    public static final double departure(double latitude1, double latitude2)
    {
        checkLatitude(latitude1);
        checkLatitude(latitude2);
        return departure((latitude1+latitude2)/2);
    }
    /**
     * Returns departure of latitude
     * <p>Departure is used in calculating distances in longitude. While 
     * (lat1-lat2)/60 is distance in NM, departure*(lon1-lon2)/60 is also 
     * distance in NM
     * 
     * @param latitude
     * @return 
     */
    public static final double departure(double latitude)
    {
        checkLatitude(latitude);
        return Math.cos(Math.toRadians(latitude));
    }
    /**
     * Return bearing from wp1 to wp2 in degrees
     * @param wp1
     * @param wp2
     * @return Degrees
     */
    public static final double bearing(WayPoint wp1, WayPoint wp2)
    {
        double lat1 = wp1.getLatitude();
        double lat2 = wp2.getLatitude();
        double lon1 = wp1.getLongitude();
        double lon2 = wp2.getLongitude();
        return bearing(lat1, lon1, lat2, lon2);
    }
    /**
     * Returns bearing from (lat1, lon1) to (lat2, lon2) in degrees
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return 
     */
    public static final double bearing(double lat1, double lon1, double lat2, double lon2)
    {
        return Math.toDegrees(radBearing(lat1, lon1, lat2, lon2));
    }
    /**
     * Returns bearing from (lat1, lon1) to (lat2, lon2) in radians
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return 
     */
    public static final double radBearing(double lat1, double lon1, double lat2, double lon2)
    {
        checkLatitude(lat1);
        checkLongitude(lon1);
        checkLatitude(lat2);
        checkLongitude(lon2);
        double dep = departure(lat1, lat2);
        double aa = dep*(lon2-lon1);
        double bb = lat2-lat1;
        double dd = Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return dd;
    }
    /**
     * Return distance between wp1 and wp2 in NM
     * @param wp1
     * @param wp2
     * @return NM
     */
    public static final double distance(WayPoint wp1, WayPoint wp2)
    {
        double lat1 = wp1.getLatitude();
        double lat2 = wp2.getLatitude();
        double lon1 = wp1.getLongitude();
        double lon2 = wp2.getLongitude();
        return distance(lat1, lon1, lat2, lon2);
    }
    /**
     * Returns distance between (lat1, lon1) and (lat2, lon2) in NM
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return 
     */
    public static final double distance(double lat1, double lon1, double lat2, double lon2)
    {
        checkLatitude(lat1);
        checkLongitude(lon1);
        checkLatitude(lat2);
        checkLongitude(lon2);
        if (
                (Math.abs(lat1 - lat2) > 100) ||
                (Math.abs(lon1 - lon2) > 100)
                )
        {
            return GreatCircle.distance(lat1, lon1, lat2, lon2);
        }
        double dep = departure(lat1, lat2);
        return 60*Math.hypot(
                lat1-lat2,
                dep*(lon1-lon2)
                );
    }
    /**
     * Returns the speed needed to move from wp1 to wp2
     * @param wp1
     * @param wp2
     * @return Kts
     */
    public static final double speed(WayPoint wp1, WayPoint wp2)
    {
        double lat1 = wp1.getLatitude();
        double lat2 = wp2.getLatitude();
        double lon1 = wp1.getLongitude();
        double lon2 = wp2.getLongitude();
        long time1 = wp1.getTime();
        long time2 = wp2.getTime();
        return speed(time1, lat1, lon1, time2, lat2, lon2);
    }
    /**
     * Returns the speed needed to move from (time1, lat1, lon1) to (time2, lat2, lon2)
     * @param time1
     * @param lat1
     * @param lon1
     * @param time2
     * @param lat2
     * @param lon2
     * @return Knots
     */
    public static final double speed(long time1, double lat1, double lon1, long time2, double lat2, double lon2)
    {
        checkLatitude(lat1);
        checkLongitude(lon1);
        checkLatitude(lat2);
        checkLongitude(lon2);
        double distance = distance(lat1, lon1, lat2, lon2);
        double duration = time2-time1;
        double hours = duration/3600000.0;
        if (hours == 0 && distance == 0)
        {
            return 0;
        }
        double speed = distance/hours;
        return speed;
    }
    /**
     * Adds delta to longitude. Positive delta is to east
     * @param longitude
     * @param delta
     * @return 
     */
    public static final double addLongitude(double longitude, double delta)
    {
        double gha = longitudeToGHA(longitude);
        gha -= delta;
        return ghaToLongitude(normalizeAngle(gha));
    }
    /**
     * Converts longitude (-180-180) to global hour angle (0-360)
     * @param longitude
     * @return 
     */
    public static final double longitudeToGHA(double longitude)
    {
        if (longitude < 0)
        {
            return -longitude;
        }
        else
        {
            return 360-longitude;
        }
    }
    /**
     * Converts global hour angle to longitude
     * @param gha in degrees
     * @return 
     */
    public static final double ghaToLongitude(double gha)
    {
        if (gha < 180)
        {
            return -gha;
        }
        else
        {
            return 360-gha;
        }
    }
    /**
     * Normalizes angle to be in 0 - 360
     * @param deg in degrees
     * @return 
     */
    public static final double normalizeAngle(double deg)
    {
        if (deg < 0)
        {
            deg %= 360;
            deg += 360;
        }
        return deg % 360;
    }
    /**
     * Normalizes angle to be in -180 - 180
     * @param deg in degrees
     * @return 
     */
    public static final double normalizeToHalfAngle(double deg)
    {
        double norm = normalizeAngle(deg);
        if (norm > 180)
        {
            return norm - 360;
        }
        else
        {
            return norm;
        }
    }
    /**
     * @deprecated Use UnitType 
     * Converts fathoms to meters
     * @param fathom
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double fathomsToMeters(double fathom)
    {
        return fathom*FathomInMeters;
    }
    /**
     * @deprecated Use UnitType 
     * Converts meters to fathoms
     * @param meters
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double metersToFathoms(double meters)
    {
        return meters/FathomInMeters;
    }
    /**
     * @deprecated Use UnitType 
     * Converts feet to  meters
     * @param feets
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double feetsToMeters(double feets)
    {
        return feets*FeetInMeters;
    }
    /**
     * @deprecated Use UnitType 
     * Converts meters to feet
     * @param meters
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double metersToFeets(double meters)
    {
        return meters/FeetInMeters;
    }
    /**
     * @deprecated Use UnitType 
     * Converts knots to m/s
     * @param knots
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double knotsToMetersPerSecond(double knots)
    {
        return NMInMetersPerHoursInSecond*knots;
    }
    /**
     * @deprecated Use UnitType 
     * Converts m/s to knots
     * @param ms
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double metersPerSecondToKnots(double ms)
    {
        return NMInMetersPerHoursInSecond/ms;
    }

    private static final double HoursInSecondPerKilo = HoursInSeconds / Kilo;
    /**
     * @deprecated Use UnitType 
     * Converts m/s to Km/h
     * @param metersPerSecond
     * @return 
     * @see org.vesalainen.math.UnitType#convert(double, org.vesalainen.math.UnitType, org.vesalainen.math.UnitType) 
     */
    public static final double metersPerSecondToKiloMetersInHour(double metersPerSecond)
    {
        return HoursInSecondPerKilo*metersPerSecond;
    }
    /**
     * Converts Km/h to m/s
     * @param kmh
     * @return 
     */
    public static final double kiloMetersInHourToMetersPerSecond(double kmh)
    {
        return kmh/HoursInSecondPerKilo;
    }
    /**
     * @deprecated Use normalizeToHalfAngle
     * Convert full angle to signed angle -180 - 180. 340 -&gt; -20
     * @param angle in degrees
     * @return
     */
    public  static final double signed(double angle)
    {
        angle = normalizeAngle(angle);
        if (angle > 180)
        {
            return angle - 360;
        }
        else
        {
            return angle;
        }
    }
    /**
     * @deprecated Use normalizeAngle
     * @param angle in degrees
     * @return angle normalized between 0 - 360 degrees
     */
    public  static final double normalizeToFullAngle(double angle)
    {
        if (angle > 360)
        {
            angle %= 360;
        }
        while (angle < 0)
        {
            angle = 360 + angle;
        }
        assert angle >= 0 && angle <= 360;
        return angle;
    }
    /**
     * @param anAngle1 in degrees
     * @param anAngle2 in degrees
     * @return Angle difference normalized between 0 - 180 degrees. If anAngle2 is right to anAngle1 returns + signed
     */
    public static final double angleDiff(double anAngle1, double anAngle2)
    {
        double angle;
        anAngle1 = normalizeAngle(anAngle1);
        anAngle2 = normalizeAngle(anAngle2);
        angle = anAngle2 - anAngle1;
        angle = normalizeAngle(angle);
        return signed(angle);
    }

    private static void checkLatitude(double latitude)
    {
        if (latitude > 90 || latitude < -90)
        {
            throw new IllegalArgumentException("latitude out of range "+latitude);
        }
    }
    private static void checkLongitude(double longitude)
    {
        if (longitude > 180 || longitude < -180)
        {
            throw new IllegalArgumentException("longitude out of range "+longitude);
        }
    }
}
