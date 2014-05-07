/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author tkv
 */
public class Distance extends Scalar
{
    public Distance(double meters)
    {
        super(meters, ScalarType.DISTANCE);
    }
    
    public Velocity getSpeed(TimeSpan timeSpan)
    {
        return new Velocity(this, timeSpan);
    }
    
    public Velocity getHullSpeed()
    {
        return new Knots(1.34*Math.sqrt(getFeets()));
    }
    
    public TimeSpan getTimeSpan(Velocity velocity)
    {
        return new TimeSpan(getMeters()/velocity.getMetersInSecond(), TimeUnit.SECONDS);
    }

    public double getMeters()
    {
        return _value;
    }

    public double getMiles()
    {
        return _value/NM_IN_METERS;
    }
    
    public double getFeets()
    {
        return _value/FEET_IN_METERS;
    }
    
    @Override
    public String toString()
    {
        return getMeters()+"m";
    }

}
