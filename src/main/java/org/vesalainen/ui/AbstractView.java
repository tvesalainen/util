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
    protected double width = Double.NaN;
    protected double height = Double.NaN;
    protected double xMax = Double.NaN;
    protected double yMax = Double.NaN;
    protected double xMin = Double.NaN;
    protected double yMin = Double.NaN;
    protected double xOff;
    protected double yOff;
    protected double scale;
    protected boolean calculated;

    public AbstractView()
    {
    }

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
        if (this.width != width)
        {
            this.width = width;
            calculated = false;
        }
        if (this.height != height)
        {
            this.height = height;
            calculated = false;
        }
    }
    private void calculate()
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
        calculated = true;
    }
    /**
     * Returns true if both rect and screen have proper values;
     * @return 
     */
    public boolean isReady()
    {
        return 
            !Double.isNaN(width) &&
            !Double.isNaN(height) &&
            !Double.isNaN(xMin) &&
            !Double.isNaN(xMax) &&
            !Double.isNaN(yMin) &&
            !Double.isNaN(yMax)
                ;
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
        calculated = false;
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void update(double x, double y)
    {
        if (x < xMin || Double.isNaN(xMin))
        {
            xMin = x;
            calculated = false;
        }
        if (x > xMax || Double.isNaN(xMax))
        {
            xMax = x;
            calculated = false;
        }
        if (y < yMin || Double.isNaN(yMin))
        {
            yMin = y;
            calculated = false;
        }
        if (y > yMax || Double.isNaN(yMax))
        {
            yMax = y;
            calculated = false;
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
        calculated = false;
    }
    /**
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double translateX(double x)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return scale * x + xOff;
    }
    /**
     * Translates cartesian y-coordinate to screen coordinate.
     * @param y
     * @return 
     */
    public double translateY(double y)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return - scale * y + yOff;
    }
    /**
     * Scales the argument to screen scale.
     * @param d
     * @return 
     */
    public double scale(double d)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return scale * d;
    }
}
