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
import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Tide
{
    public static final long PERIOD = HOURS.toMillis(12) + MINUTES.toMillis(25);
    private static final long DEGREE_IN_MILLIS = DAYS.toMillis(1)/360;
    
    private double coef;
    private long time;
    private double angle;
    private double gha;
    /**
     * Initializes Tide
     * @param coef Coefficient to sine
     * @param time Time in milliseconds
     * @param degrees Start angle in degrees
     * @param longitude Longitude
     */
    public Tide(double coef, long time, double degrees, double longitude)
    {
        this.coef = coef;
        this.time = time;
        this.angle = Math.toRadians(degrees);
        this.gha = Navis.longitudeToGHA(longitude);
    }
    /**
     * Returns tide height (-coef - coef) at initial longitude 
     * @param millis
     * @return 
     */
    public double getTide(long millis)
    {
        return getTide(millis, Navis.ghaToLongitude(gha));
    }
    /**
     * Returns tide height (-coef - coef)
     * @param millis
     * @param longitude
     * @return 
     */
    public double getTide(long millis, double longitude)
    {
        double gha1 = Navis.longitudeToGHA(longitude);
        double ghaDif = Math.toRadians(-Navis.angleDiff(gha, gha1));
        return coef*sin(angle+ghaDif+toRadians(millis-time, MILLISECONDS));
    }
    /**
     * Returns time duration converted to tide degrees.
     * @param duration
     * @param unit
     * @return 
     */
    public static final double toDegrees(long duration, TimeUnit unit)
    {
        long millis = MILLISECONDS.convert(duration, unit);
        return millis/(PERIOD/360);
    }
    /**
     * Returns time duration converted to tide radians.
     * @param duration
     * @param unit
     * @return 
     */
    public static final double toRadians(long duration, TimeUnit unit)
    {
        long millis = MILLISECONDS.convert(duration, unit);
        return millis/(PERIOD/(2*PI));
    }
}
