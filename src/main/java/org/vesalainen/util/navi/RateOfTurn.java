/*
 * Copyright (C) 2014 Timo Vesalainen
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
 * RateOfTurn is a base class for turn.
 * @author tkv
 */
public class RateOfTurn extends Scalar
{
    public RateOfTurn()
    {
        super(ScalarType.TURN);
    }
    /**
     * 
     * @param radiansPerSecond If &lt; 0 turns left (or port)
     */
    public RateOfTurn(double radiansPerSecond)
    {
        super(radiansPerSecond, ScalarType.TURN);
    }

    public RateOfTurn(Scalar scalar)
    {
        super(scalar);
    }

    public boolean isRight()
    {
        return value >= 0;
    }
    /**
     * Return ROT in degrees / minute
     * @return ROT in degrees / minute
     */
    public double getDegreesPerMinute()
    {
        return Math.toDegrees(Math.abs(value))*60;
    }
    /**
     * Returns TimeSpan for full circle
     * @return 
     */
    public TimeSpan getTimeForFullCircle()
    {
        return new TimeSpan(getSecondsForFullCircle(), TimeUnit.SECONDS);
    }
    /**
     * Return how many seconds it takes for full circle.
     * @return 
     */
    public double getSecondsForFullCircle()
    {
        return 2*Math.PI / Math.abs(value);
    }
    /**
     * Returns the radius of the circle
     * @param velocity Speed of the vessel
     * @return 
     */
    public Distance getRadius(Velocity velocity)
    {
        Distance circle = velocity.getDistance(getTimeForFullCircle());
        circle.mul(1/Math.PI);
        return circle;
    }
    public Angle getBearingAfter(Angle bearing, TimeSpan span)
    {
        return bearing.add(getAngleChange(span));
    }
    /**
     * Return's Angle change after span
     * @param span TimeSpan of turning
     * @return 
     */
    public Angle getAngleChange(TimeSpan span)
    {
        return new Angle(span.getSeconds()*value);
    }
    /**
     * Return's motion after timespan
     * @param motion Motion at the beginning
     * @param span TimeSpan of turning
     * @return 
     */
    public Motion getMotionAfter(Motion motion, TimeSpan span)
    {
        return new Motion(motion.getSpeed(), getBearingAfter(motion.getAngle(), span));
    }
}
