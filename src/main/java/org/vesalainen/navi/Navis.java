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
package org.vesalainen.navi;

import java.util.concurrent.TimeUnit;

/**
 * Collection of navigational methods etc.
 * 
 * <p>coordinates are in degrees. Positive values are north and east.
 * 
 * @author tkv
 */
public class Navis
{
    public static final double Kilo = 1000;
    public static final double NMInMeters = 1852;
    public static final double FeetInMeters = 0.3048;
    public static final double FathomInMeters = 1.8288;
    public static final double HoursInSecond = TimeUnit.HOURS.toSeconds(1);
    public static final double NMInMetersPerHoursInSecond = NMInMeters / HoursInSecond;
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
        return Math.toDegrees(dd);
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
        double speed = distance/hours;
        return speed;
    }
    /**
     * Converts fathoms to meters
     * @param fathom
     * @return 
     */
    public static final double fathomsToMeters(double fathom)
    {
        return fathom*FathomInMeters;
    }
    /**
     * Converts meters to fathoms
     * @param meters
     * @return 
     */
    public static final double metersToFathoms(double meters)
    {
        return meters/FathomInMeters;
    }
    /**
     * Converts feet to  meters
     * @param feets
     * @return 
     */
    public static final double feetsToMeters(double feets)
    {
        return feets*FeetInMeters;
    }
    /**
     * Converts meters to feet
     * @param meters
     * @return 
     */
    public static final double metersToFeets(double meters)
    {
        return meters/FeetInMeters;
    }
    /**
     * Converts knots to m/s
     * @param knots
     * @return 
     */
    public static final double knotsToMetersPerSecond(double knots)
    {
        return NMInMetersPerHoursInSecond*knots;
    }
    /**
     * Converts m/s to knots
     * @param ms
     * @return 
     */
    public static final double metersPerSecondToKnots(double ms)
    {
        return NMInMetersPerHoursInSecond/ms;
    }

    private static final double HoursInSecondPerKilo = HoursInSecond / Kilo;
    /**
     * Converts m/s to Km/h
     * @param metersPerSecond
     * @return 
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
