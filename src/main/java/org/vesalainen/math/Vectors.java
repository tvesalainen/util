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

/**
 *
 * @author Timo Vesalainen
 */
public final class Vectors
{
    /**
     * Returns true if vector (x2, y2) is clockwise of (x1, y1)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static final boolean isClockwise(double x1, double y1, double x2, double y2)
    {
        return -x1*y2+x2*y1 > 0;
    }
    /**
     * Returns true if vector (x2, y2) is clockwise of (x1, y1) in (ox, oy) centered
     * coordinate.
     * @param ox
     * @param oy
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static final boolean isClockwise(double ox, double oy, double x1, double y1, double x2, double y2)
    {
        return isClockwise(x1-ox, y1-oy, x2-ox, y2-oy);
    }
    /**
     * Returns true if vectors (x1, y1) and (x2, y2) are aligned (ox, oy) centered
     * coordinate.
     * @param ox
     * @param oy
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static final boolean areAligned(double ox, double oy, double x1, double y1, double x2, double y2)
    {
        return areAligned(x1-ox, y1-oy, x2-ox, y2-oy);
    }
    /**
     * Returns true if vectors (x1, y1) and (x2, y2) are aligned
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static final boolean areAligned(double x1, double y1, double x2, double y2)
    {
        return -x1*y2+x2*y1 == 0;
    }
}
