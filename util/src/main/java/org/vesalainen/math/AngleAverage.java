/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import org.vesalainen.navi.Navis;

/**
 * Calculates angle average.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleAverage implements Average
{
    private double sin;
    private double cos;
    /**
     * Adds angle in degrees
     * @param deg 
     */
    public void addDeg(double deg)
    {
        addDeg(deg, 1.0);
    }
    /**
     * Adds angle in degrees with weight
     * @param deg
     * @param weight 
     */
    public void addDeg(double deg, double weight)
    {
        add(Math.toRadians(deg), weight);
    }
    /**
     * Add angle in radians
     * @param rad 
     */
    public void add(double rad)
    {
        add(rad, 1.0);
    }
    /**
     * Adds angle in radians with weight
     * @param rad
     * @param weight 
     */
    public void add(double rad, double weight)
    {
        sin += Math.sin(rad)*weight;
        cos += Math.cos(rad)*weight;
    }
    /**
     * Returns average in degrees 0 - 360
     * @return 
     */
    public double averageDeg()
    {
        return Navis.normalizeToFullAngle(Math.toDegrees(average()));
    }
    /**
     * Returns average in radians -pi - pi
     * @return 
     */
    @Override
    public double average()
    {
        return Math.atan2(sin, cos);
    }
    /**
     * Returns average in radians
     * @return 
     */
    @Override
    public double fast()
    {
        return Math.atan2(sin, cos);
    }

    @Override
    public String toString()
    {
        return "AngleAverage{" + averageDeg() + "deg }";
    }
    
}
