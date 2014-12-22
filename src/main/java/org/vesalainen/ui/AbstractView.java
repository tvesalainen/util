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

import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Rect;


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
    protected double xMax = Double.NEGATIVE_INFINITY;
    protected double yMax = Double.NEGATIVE_INFINITY;
    protected double xMin = Double.POSITIVE_INFINITY;
    protected double yMin = Double.POSITIVE_INFINITY;
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
            !Double.isInfinite(xMin) &&
            !Double.isInfinite(xMax) &&
            !Double.isInfinite(yMin) &&
            !Double.isInfinite(yMax)
                ;
    }
    /**
     * Resets the limits.
     */
    public void reset()
    {
        xMin = Double.POSITIVE_INFINITY;
        xMax = Double.NEGATIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        calculated = false;
    }
    public void updatePolygon(Polygon polygon)
    {
        updateRect(polygon.bounds);
    }
    public void updatePolygon(DenseMatrix64F polygon)
    {
        int len = polygon.numRows;
        double[] d = polygon.data;
        for (int r=0;r<len;r++)
        {
            updatePoint(d[2*r], d[2*r+1]);
        }
    }
    public void updateRect(Rect bounds)
    {
        updatePoint(bounds.xMax, bounds.yMax);
        updatePoint(bounds.xMin, bounds.yMax);
        updatePoint(bounds.xMin, bounds.yMin);
        updatePoint(bounds.xMax, bounds.yMin);
    }
    public void updateCircle(Circle c)
    {
        updateCircle(c.getX(), c.getY(), c.getRadius());
    }

    /**
     * Updates limits so that circle is visible.
     * @param x
     * @param y
     * @param radius 
     */
    public void updateCircle(double x, double y, double radius)
    {
        updatePoint(x, y);
        updatePoint(x-radius, y-radius);
        updatePoint(x+radius, y-radius);
        updatePoint(x-radius, y+radius);
        updatePoint(x+radius, y+radius);
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void updatePoint(double x, double y)
    {
        if (x < xMin)
        {
            xMin = x;
            calculated = false;
        }
        if (x > xMax)
        {
            xMax = x;
            calculated = false;
        }
        if (y < yMin)
        {
            yMin = y;
            calculated = false;
        }
        if (y > yMax)
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
    public final void setRect(double x, double y, double r)
    {
        this.xMin = x - r;
        this.xMax = x + r;
        this.yMin = y - r;
        this.yMax = y + r;
        calculated = false;
    }
    /**
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double toScreenX(double x)
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
    public double toScreenY(double y)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return - scale * y + yOff;
    }
    /**
     * Translates screen x-coordinate to cartesian coordinate.
     * @param x
     * @return 
     */
    public double fromScreenX(double x)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return (x - xOff) / scale;
    }
    /**
     * Translates screen y-coordinate to cartesian coordinate.
     * @param y
     * @return 
     */
    public double fromScreenY(double y)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return - (y - yOff) / scale;
    }
    /**
     * Scales the argument to screen scaleToScreen.
     * @param d
     * @return 
     */
    public double scaleToScreen(double d)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return d * scale;
    }

    public double scaleFromScreen(double d)
    {
        assert isReady();
        if (!calculated)
        {
            calculate();
        }
        return d / scale;
    }

}
