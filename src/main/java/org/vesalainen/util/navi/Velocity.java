/*
 * Copyright (C) 2011 Timo Vesalainen
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

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author tkv
 */
public class Velocity extends Scalar
{
    public static final Velocity NaN = new Velocity(Double.NaN);

    public Velocity(double metersInSecond)
    {
        super(metersInSecond, ScalarType.VELOCITY);
    }

    public Velocity(Distance distance, TimeSpan timeSpan)
    {
        super(distance.getMeters()/timeSpan.getSeconds(), ScalarType.VELOCITY);
    }

    public Velocity()
    {
        super(ScalarType.VELOCITY);
    }

    public Knots asKnots()
    {
        return new Knots(getKnots());
    }
    /**
     * Return the distance moved during timespan
     * @param timeSpan
     * @return
     */
    public Distance getDistance(TimeSpan timeSpan)
    {
        return new Distance(value*timeSpan.getSeconds());
    }
    /**
     * Returns the time span taken to move the distance
     * @param distance
     * @return
     */
    public TimeSpan getTimeSpan(Distance distance)
    {
        return new TimeSpan((long)(1000*distance.getMeters()/value), TimeUnit.MILLISECONDS);
    }
    /**
     * Returns the time we are there if we started at start
     * @param start Starting time
     * @param distance Distance to meve
     * @return
     */
    public Date getThere(Date start, Distance distance)
    {
        TimeSpan timeSpan = getTimeSpan(distance);
        return timeSpan.addDate(start);
    }
    
    public double getMetersInSecond()
    {
        return value;
    }

    public double getKiloMetersInHour()
    {
        return TimeUnit.HOURS.toSeconds(1)*value/Distance.KILO;
    }

    public double getKnots()
    {
        return TimeUnit.HOURS.toSeconds(1)*value/Distance.NM_IN_METERS;
    }

    @Override
    public String toString()
    {
        return String.format("%.1f m/s", getMetersInSecond());
    }

}
