/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import org.vesalainen.util.navi.ScalarType;
import org.vesalainen.util.navi.Scalar;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Distance;
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
        return new Distance(_value*timeSpan.getSeconds());
    }
    /**
     * Returns the time span taken to move the distance
     * @param distance
     * @return
     */
    public TimeSpan getTimeSpan(Distance distance)
    {
        return new TimeSpan((long)(1000*distance.getMeters()/_value), TimeUnit.MILLISECONDS);
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
        return _value;
    }

    public double getKiloMetersInHour()
    {
        return TimeUnit.HOURS.toSeconds(1)*_value/Distance.KILO;
    }

    public double getKnots()
    {
        return TimeUnit.HOURS.toSeconds(1)*_value/Distance.NM_IN_METERS;
    }

    @Override
    public String toString()
    {
        return String.format("%.1f m/s", getMetersInSecond());
    }

}
