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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Rect;


/**
 * The AbstractView class translates cartesian coordinates to screen coordinates.
 * In cartesian coordinates y grows up, while in screen coordinates y grows down.
 * 
 * <p>Use setScreen to set screen size. setRect sets the limits to used 
 * coordinates. Translation maintains aspect ratio if keepAspectRatio=true.
 * @author Timo Vesalainen
 */
public class AbstractView
{
    protected double width = Double.NaN;
    protected double height = Double.NaN;
    protected double xMax;
    protected double yMax;
    protected double xMin;
    protected double yMin;
    protected double xOff;
    protected double yOff;
    protected double scaleX;
    protected double scaleY;
    protected boolean calculated;
    protected boolean keepAspectRatio;
    protected AffineTransform transform = new AffineTransform();
    private ThreadLocal<Point2D> srcPnt = ThreadLocal.withInitial(Point2D.Double::new);
    private ThreadLocal<Point2D> dstPnt = ThreadLocal.withInitial(Point2D.Double::new);
    /**
     * Creates AbstractView which keeps aspect-ratio. Initial size is zero.
     */
    public AbstractView()
    {
        this(true);
    }
    /**
     * Creates AbstractView with given aspect-ratio. Initial size is zero.
     * @param keepAspectRatio 
     */
    public AbstractView(boolean keepAspectRatio)
    {
        this(keepAspectRatio, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }
    /**
     * Creates AbstractView which keeps aspect-ratio with given initial size.
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax 
     */
    public AbstractView(double xMin, double xMax, double yMin, double yMax)
    {
        this(true, xMin, xMax, yMin, yMax);
    }
    /**
     * Creates AbstractView with given aspect-ratio with given initial size.
     * @param keepAspectRatio
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax 
     */
    public AbstractView(boolean keepAspectRatio, double xMin, double xMax, double yMin, double yMax)
    {
        this.keepAspectRatio = keepAspectRatio;
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
        if (!calculated)
        {
            check();
            double aspect = width / height;
            double xyWidth = xMax - xMin;
            double xyHeight = yMax - yMin;
            if (keepAspectRatio)
            {
                double xyAspect = xyWidth / xyHeight;
                if (aspect > xyAspect)
                {
                    scaleX = scaleY = height / xyHeight;
                    xOff = -scaleY*xMin + (width - scaleY*xyWidth) / 2.0;
                    yOff = scaleY*yMin + height;
                }
                else
                {
                    scaleX = scaleY = width / xyWidth;
                    xOff = -scaleY*xMin;
                    yOff = scaleY*yMin + height / 2.0 + scaleY*xyHeight / 2.0;
                }
            }
            else
            {
                scaleX = width / xyWidth;
                scaleY = height / xyHeight;
                xOff = -scaleX*xMin;
                yOff = scaleY*yMin + height / 2.0 + scaleY*xyHeight / 2.0;
            }
            transform.setTransform(scaleX, 0, 0, -scaleY, xOff, yOff);
            calculated = true;
        }
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
        updatePolygon(polygon.data, polygon.numRows);
    }
    public void updatePolygon(double[] data, int length)
    {
        for (int r=0;r<length;r++)
        {
            updatePoint(data[2*r], data[2*r+1]);
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
     * Adds margin to visible screen. 
     * @param m Margin m*width/height
     */
    public void margin(double m)
    {
        double w = xMax-xMin;
        double h = yMax-yMin;
        setRect(
                xMin-w*m,
                xMax+w*m,
                yMin-w*m,
                yMax+w*m
        );
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void updatePoint(double x, double y)
    {
        updateX(x);
        updateY(y);
    }
    public void updateX(double x)
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
    }
    public void updateY(double y)
    {
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
     * Returns AffineTransform
     * @return 
     */
    public AffineTransform getTransform()
    {
        calculate();
        return transform;
    }
    
    /**
     * @deprecated Use AffineTransform
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double toScreenX(double x)
    {
        calculate();
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(x, 0);
        transform.transform(src, dst);
        return dst.getX();
        //return scaleX * x + xOff;
    }
    /**
     * @deprecated Use AffineTransform
     * Translates cartesian y-coordinate to screen coordinate.
     * @param y
     * @return 
     */
    public double toScreenY(double y)
    {
        calculate();
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(0, y);
        transform.transform(src, dst);
        return dst.getY();
        //return - scaleY * y + yOff;
    }
    /**
     * @deprecated Use AffineTransform
     * Translates screen x-coordinate to cartesian coordinate.
     * @param x
     * @return 
     */
    public double fromScreenX(double x)
    {
        try
        {
            calculate();
            Point2D src = srcPnt.get();
            Point2D dst = dstPnt.get();
            src.setLocation(x, 0);
            transform.inverseTransform(src, dst);
            return dst.getX();
            //return (x - xOff) / scaleX;
        }
        catch (NoninvertibleTransformException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * @deprecated Use AffineTransform
     * Translates screen y-coordinate to cartesian coordinate.
     * @param y
     * @return 
     */
    public double fromScreenY(double y)
    {
        try
        {
            calculate();
            Point2D src = srcPnt.get();
            Point2D dst = dstPnt.get();
            src.setLocation(0, y);
            transform.inverseTransform(src, dst);
            return dst.getY();
            //return - (y - yOff) / scaleY;
        }
        catch (NoninvertibleTransformException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Scales the argument to screen scaleToScreen.
     * @param d
     * @return 
     */
    public double scaleToScreen(double d)
    {
        if (!keepAspectRatio)
        {
            throw new UnsupportedOperationException("not supported with keepAspectRatio=false");
        }
        return scaleToScreenX(d);
    }
    public double scaleToScreenX(double d)
    {
        calculate();
        return d * transform.getScaleX();
    }
    public double scaleToScreenY(double d)
    {
        calculate();
        return d * transform.getScaleY();
    }
    public double scaleFromScreen(double d)
    {
        if (!keepAspectRatio)
        {
            throw new UnsupportedOperationException("not supported with keepAspectRatio=false");
        }
        calculate();
        return d / scaleY;
    }
    private void check()
    {
        if (!isReady() || width <= 0 || height <= 0)
        {
            throw new IllegalStateException("not initialized properly");
        }
    }
}
