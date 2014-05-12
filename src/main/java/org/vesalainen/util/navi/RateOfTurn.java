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
    private boolean right = true;
    public RateOfTurn()
    {
        super(ScalarType.TURN);
    }
    /**
     * 
     * @param radiansPerSecond If < 0 turns left (or port)
     */
    public RateOfTurn(double radiansPerSecond)
    {
        super(Math.abs(radiansPerSecond), ScalarType.TURN);
        if (radiansPerSecond < 0)
        {
            right = false;
        }
    }

    public RateOfTurn(Scalar scalar)
    {
        super(scalar);
    }

    public boolean isRight()
    {
        return right;
    }
    /**
     * Return ROT in degrees / minute
     * @return ROT in degrees / minute
     */
    public double getDegreesPerMinute()
    {
        return Math.toDegrees(value)*60;
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
        return 2*Math.PI / value;
    }
    /**
     * Returns the radius of the circle
     * @param velocity
     * @return 
     */
    public Distance getRadius(Velocity velocity)
    {
        Distance circle = velocity.getDistance(getTimeForFullCircle());
        circle.mul(1/Math.PI);
        return circle;
    }
}
