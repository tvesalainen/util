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
        return value;
    }

    public double getMiles()
    {
        return value/NMInMeters;
    }
    
    public double getFeets()
    {
        return value/FeetInMeters;
    }
    
    public double getFathoms()
    {
        return value/FathomInMeters;
    }
    
    @Override
    public String toString()
    {
        return getMeters()+"m";
    }

}
