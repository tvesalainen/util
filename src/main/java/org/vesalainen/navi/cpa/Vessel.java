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

import org.vesalainen.navi.Navis;

/**
 * A base class for calculating collision point of two vessels that are possibly
 * turning.
 * 
 * @author tkv
 */
public class Vessel
{
    private static final double Pi2 = 2*Math.PI;
    // last updated
    protected long time;
    protected double latitude;
    protected double longitude;
    protected double speed;         // knots
    protected double bearing;       // degrees
    protected double rateOfTurn;    // degrees / minute; negative to port
    // previous update
    protected long prevTime;
    protected double prevLatitude;
    protected double prevLongitude;
    // calculated
    protected double centerLatitude;
    protected double centerLongitude;
    protected double radius;
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
            this.speed = Navis.speed(prevTime, prevLatitude, prevLongitude, time, latitude, longitude);
            this.bearing = Navis.bearing(prevLatitude, prevLongitude, latitude, longitude);
            this.rateOfTurn = rateOfTurn;
            calc();
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
        calc();
    }

    private void calc()
    {
        double hoursForFullCircle = (360 / Math.abs(rateOfTurn))/60;
        radius = speed*hoursForFullCircle/Pi2;
        double b = Navis.normalizeAngle(bearing+90*Math.signum(rateOfTurn));
        centerLatitude = latitude+Navis.deltaLatitude(radius, b);
        centerLongitude = longitude+Navis.deltaLongitude(latitude, radius, b);
    }

    public double getRadius()
    {
        return radius;
    }

    public double getCenterLatitude()
    {
        return centerLatitude;
    }

    public double getCenterLongitude()
    {
        return centerLongitude;
    }
    
}
