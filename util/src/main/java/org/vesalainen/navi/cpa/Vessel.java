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

import static org.vesalainen.navi.Navis.*;

/**
 * A base class for calculating collision point of two vessels that are possibly
 * turning.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Vessel
{
    private static final double Pi2 = 2*Math.PI;
    // last updated
    protected long time;
    protected double latitude;
    protected double longitude;
    protected double speed;         // knots
    protected double bearing = Double.NaN;       // degrees
    protected double rateOfTurn;    // degrees / minute; negative to port
    // previous update
    protected long prevTime;
    protected double prevLatitude;
    protected double prevLongitude;
    // calculated
    protected double centerLatitude = Double.NaN;
    protected double centerLongitude = Double.NaN;
    protected double radius = Double.NaN;
    protected boolean calculated;
    /**
     * Updates values and calculates speed and bearing. After that calculates other
     * data.
     * 
     * <p>Don't mix short and long update method usage in same instance!
     * 
     * @param time
     * @param latitude
     * @param longitude
     * @param rateOfTurn Degrees / minute
     */
    public void update(long time, double latitude, double longitude, double rateOfTurn)
    {
        if (prevTime > 0)
        {
            this.time = time;
            this.latitude = latitude;
            this.longitude = longitude;
            this.speed = speed(prevTime, prevLatitude, prevLongitude, time, latitude, longitude);
            this.bearing = bearing(prevLatitude, prevLongitude, latitude, longitude);
            this.rateOfTurn = rateOfTurn;
            calculated = false;
        }
        prevTime = time;
        prevLatitude = latitude;
        prevLongitude = longitude;
    }
    /**
     * Updates values. After that calculates other
     * data.
     * 
     * <p>Don't mix short and long update method usage in same instance!
     * 
     * @param time
     * @param latitude
     * @param longitude
     * @param speed
     * @param bearing Degrees
     * @param rateOfTurn Degrees / minute
     */
    public void update(long time, double latitude, double longitude, double speed, double bearing, double rateOfTurn)
    {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.bearing = bearing;
        this.rateOfTurn = rateOfTurn;
        calculated = false;
    }

    private void calc()
    {
        if (Double.isNaN(bearing))
        {
            throw new IllegalStateException("not updated");
        }
        if (!calculated)
        {
            double arot = Math.abs(rateOfTurn);
            if (arot > 0)
            {
                double hoursForFullCircle = (360 / arot)/60;
                radius = speed*hoursForFullCircle/Pi2;
                double b = normalizeAngle(bearing+90*Math.signum(rateOfTurn));
                centerLatitude = latitude+deltaLatitude(radius, b);
                centerLongitude = addLongitude(longitude, deltaLongitude(latitude, radius, b));
            }
            else
            {
                radius = Double.NaN;
                centerLatitude = Double.NaN;
                centerLongitude = Double.NaN;
            }
            calculated = true;
        }
    }
    public static final double estimatedDistance(Vessel v1, Vessel v2, long et)
    {
        return distance(
                v1.estimatedLatitude(et),
                v1.estimatedLongitude(et),
                v2.estimatedLatitude(et),
                v2.estimatedLongitude(et)
        );
    }
    /**
     * Returns estimated latitude at et
     * @param et
     * @return 
     */
    public final double estimatedLatitude(long et)
    {
        calc();
        if (rateOfTurn == 0)
        {
            double dist = calcDist(et);
            return latitude+deltaLatitude(dist, bearing);
        }
        else
        {
            double deg = calcDeg(et);
            return centerLatitude+deltaLatitude(radius, deg);
        }
    }
    /**
     * Return estimated longitude at et
     * @param et
     * @return 
     */
    public final double estimatedLongitude(long et)
    {
        calc();
        if (rateOfTurn == 0)
        {
            double dist = calcDist(et);
            return addLongitude(longitude, deltaLongitude(latitude, dist, bearing));
        }
        else
        {
            double deg = calcDeg(et);
            return addLongitude(centerLongitude, deltaLongitude(latitude, radius, deg));
        }
    }
    private double calcDist(long et)
    {
        if (et < time)
        {
            throw new IllegalArgumentException("cannot estimate past");
        }
        double dh = (et-time)/3600000.0;
        return speed*dh;
    }
    private double calcDeg(long et)
    {
        if (et < time)
        {
            throw new IllegalArgumentException("cannot estimate past");
        }
        double dm = (et-time)/60000.0;
        return normalizeAngle(bearing-90*Math.signum(rateOfTurn)+rateOfTurn*dm);
    }
    public double getRadius()
    {
        calc();
        return radius;
    }

    public double getCenterLatitude()
    {
        calc();
        return centerLatitude;
    }

    public double getCenterLongitude()
    {
        calc();
        return centerLongitude;
    }
    
}
