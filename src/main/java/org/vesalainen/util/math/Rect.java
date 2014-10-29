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

package org.vesalainen.util.math;

/**
 *
 * @author Timo Vesalainen
 */
public class Rect
{
    public double xMax = Double.NaN;
    public double yMax = Double.NaN;
    public double xMin = Double.NaN;
    public double yMin = Double.NaN;
    
    public boolean isHit(double x, double y)
    {
        return !(x > xMax || x < xMin || y > yMax || y < yMin);
    }
    public double getWidth()
    {
        return xMax - xMin;
    }
    public double getHeight()
    {
        return yMax - yMin;
    }
    /**
     * Resets the limits.
     */
    public void reset()
    {
        xMin = Double.NaN;
        xMax = Double.NaN;
        yMin = Double.NaN;
        yMax = Double.NaN;
    }
    /**
     * Updates limits so that circle is visible.
     * @param x
     * @param y
     * @param radius 
     */
    public void update(double x, double y, double radius)
    {
        update(x, y);
        update(x-radius, y-radius);
        update(x+radius, y-radius);
        update(x-radius, y+radius);
        update(x+radius, y+radius);
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void update(double x, double y)
    {
        boolean changed = false;
        if (x < xMin || Double.isNaN(xMin))
        {
            xMin = x;
            changed = true;
        }
        if (x > xMax || Double.isNaN(xMax))
        {
            xMax = x;
            changed = true;
        }
        if (y < yMin || Double.isNaN(yMin))
        {
            yMin = y;
            changed = true;
        }
        if (y > yMax || Double.isNaN(yMax))
        {
            yMax = y;
            changed = true;
        }
    }
    /**
     * Sets the visible rectangle of translated coordinates.
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax 
     */
    public final void setRect(double xMin, double xMax, double yMin, double yMax)
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
}
