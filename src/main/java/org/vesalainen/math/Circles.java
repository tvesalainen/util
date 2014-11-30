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

package org.vesalainen.math;

import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class Circles
{
    /**
     * Returns points (x, y) angle to the circle center in radians.
     * @param circle
     * @param x
     * @param y
     * @return 
     */
    public static double angle(Circle circle, double x, double y)
    {
        return angle(circle.getX(), circle.getY(), x, y);
    }
    public static double angle(double x1, double y1, double x2, double y2)
    {
        return Angle.normalizeToFullAngle(Math.atan2(y2-y1, x2-x1));
    }
    /**
     * Returns true if point (x, y) is inside circle
     * @param circle
     * @param x
     * @param y
     * @return 
     */
    public static boolean isInside(Circle circle, double x, double y)
    {
        return distanceFromCenter(circle, x, y) < circle.getRadius();
    }
    /**
     * Returns true if c2 is inside of c1
     * @param c1
     * @param c2
     * @return 
     */
    public static boolean isInside(Circle c1, Circle c2)
    {
        return distanceFromCenter(c1, c2.getX(), c2.getY())+c2.getRadius() < c1.getRadius();
    }
    /**
     * Returns point (x, y) distance from circles center.
     * @param circle
     * @param x
     * @param y
     * @return 
     */
    public static double distanceFromCenter(Circle circle, double x, double y)
    {
        return Math.hypot(x-circle.getX(), y-circle.getY());
    }
    public static double distance(double x1, double y1, double x2, double y2)
    {
        return Math.hypot(x1-x2, y1-y2);
    }
}
