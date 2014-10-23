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

package org.vesalainen.ui;


/**
 * The AbstractView class translates cartesian coordinates to screen coordinates.
 * In cartesian coordinates y grows up, while in screen coordinates y grows down.
 * 
 * <p>Use setScreen to set screen size. setRect sets the limits to used 
 * coordinates. Translation maintains aspect ratio.
 * @author Timo Vesalainen
 */
public class AbstractView
{
    protected double xMax;
    protected double yMax;
    protected double xMin;
    protected double yMin;
    protected double xOff;
    protected double yOff;
    protected double scale;

    public AbstractView(double xMin, double xMax, double yMin, double yMax)
    {
        setRect(xMin, xMax, yMin, yMax);
    }
    /**
     * Sets the screen size.
     * @param width
     * @param height 
     */
    public void setScreen(double width, double height)
    {
        double aspect = width / height;
        double xyWidth = xMax - xMin;
        double xyHeight = yMax - yMin;
        double xyAspect = xyWidth / xyHeight;
        if (aspect > xyAspect)
        {
            scale = height / xyHeight;
            xOff = -scale*xMin + (width - scale*xyWidth) / 2.0;
            yOff = scale*yMin + height;
        }
        else
        {
            scale = width / xyWidth;
            xOff = -scale*xMin;
            yOff = scale*yMin + height / 2.0 + scale*xyHeight / 2.0;
        }
    }
    /**
     * Resets the limits.
     */
    public void reset()
    {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void update(double x, double y)
    {
        if (x < xMin)
        {
            xMin = x;
        }
        if (x > xMax)
        {
            xMax = x;
        }
        if (y < yMin)
        {
            yMin = y;
        }
        if (y > yMax)
        {
            yMax = y;
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
    /**
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double translateX(double x)
    {
        return scale * x + xOff;
    }
    /**
     * Translates cartesian y-coordinate to screen coordinate.
     * @param y
     * @return 
     */
    public double translateY(double y)
    {
        return - scale * y + yOff;
    }
    /**
     * Scales the argument to screen scale.
     * @param d
     * @return 
     */
    public double scale(double d)
    {
        return scale * d;
    }
    /**
     * Sets the maximum x that is visible in screen.
     * @param xMax 
     */
    public void setXMax(double xMax)
    {
        this.xMax = xMax;
    }
    /**
     * Sets the maximum y that is visible in screen.
     * @param yMax 
     */
    public void setYMax(double yMax)
    {
        this.yMax = yMax;
    }
    /**
     * Sets the minimum x that is visible in screen.
     * @param xMin 
     */
    public void setXMin(double xMin)
    {
        this.xMin = xMin;
    }
    /**
     * Sets the minimum y that is visible in screen.
     * @param yMin 
     */
    public void setYMin(double yMin)
    {
        this.yMin = yMin;
    }

}
