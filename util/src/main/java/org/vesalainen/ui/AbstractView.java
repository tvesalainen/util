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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.stream.Stream;


/**
 * The AbstractView class translates Cartesian coordinates to screen coordinates.
 * In Cartesian coordinates y grows up, while in screen coordinates y grows down.
 * 
 * <p>Use setScreen to createScreenTransform screen size. setRect sets the limits to used 
 coordinates. Translation maintains aspect ratio if keepAspectRatio=true.
 * @author Timo Vesalainen
 */
public class AbstractView
{
    protected DoubleBounds minUserBounds = new DoubleBounds();
    protected DoubleBounds screenBounds = new DoubleBounds();
    protected DoubleBounds userBounds = new DoubleBounds();
    protected boolean keepAspectRatio;
    protected DoubleTransform transform = DoubleTransform.identity();
    protected AffineTransform affineTransform = new AffineTransform();
    private static ThreadLocal<Point2D> srcPnt = ThreadLocal.withInitial(Point2D.Double::new);
    private static ThreadLocal<Point2D> dstPnt = ThreadLocal.withInitial(Point2D.Double::new);
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
        this(keepAspectRatio, new DoubleBounds());
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
        this(keepAspectRatio, new Rectangle2D.Double(xMin, yMin, xMax-xMin, yMax-yMin));
    }
    public AbstractView(boolean keepAspectRatio, Rectangle2D.Double rect)
    {
        this.keepAspectRatio = keepAspectRatio;
        setRect(rect);
    }
    /**
     * Sets the screen size.
     * @param width
     * @param height 
     */
    public void setScreen(double width, double height)
    {
        screenBounds.setRect(0, 0, width, height);
    }
    protected void update(Stream<Shape> shapes)
    {
        if (screenBounds.isEmpty())
        {
            throw new IllegalStateException("not initialized");
        }
        userBounds.setRect(minUserBounds);
        shapes.map(Shape::getBounds2D).forEach(this::update);
        Transforms.createScreenTransform(userBounds, screenBounds, keepAspectRatio, affineTransform);
    }
    protected void update(Rectangle2D bounds)
    {
        updatePoint(bounds.getMaxX(), bounds.getMaxY());
        updatePoint(bounds.getMinX(), bounds.getMinY());
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void updatePoint(double x, double y)
    {
        transform.transform(x, y, userBounds::add);
    }
    /**
     * Adds margin to visible screen. 
     * @param m Margin m*width/height
     */
    public void margin(double m)
    {
        double xMin = userBounds.getMinX();
        double yMin = userBounds.getMinY();
        double xMax = userBounds.getMaxX();
        double yMax = userBounds.getMaxY();
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
     * Sets the visible rectangle of translated coordinates.
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax 
     */
    public final void setRect(double xMin, double xMax, double yMin, double yMax)
    {
        setRect(new Rectangle2D.Double(xMin, yMin, xMax-xMin, yMax-yMin));
    }
    public final void setRect(Rectangle2D.Double rect)
    {
        minUserBounds.setRect(rect);
    }
    /**
     * @deprecated Use AffineTransform
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double toScreenX(double x)
    {
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(x, 0);
        affineTransform.transform(src, dst);
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
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(0, y);
        affineTransform.transform(src, dst);
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
            Point2D src = srcPnt.get();
            Point2D dst = dstPnt.get();
            src.setLocation(x, 0);
            affineTransform.inverseTransform(src, dst);
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
            Point2D src = srcPnt.get();
            Point2D dst = dstPnt.get();
            src.setLocation(0, y);
            affineTransform.inverseTransform(src, dst);
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
    @Deprecated
    public double scaleToScreen(double d)
    {
        if (!keepAspectRatio)
        {
            throw new UnsupportedOperationException("not supported with keepAspectRatio=false");
        }
        return scaleToScreenX(d);
    }
    @Deprecated
    public double scaleToScreenX(double d)
    {
        return d * affineTransform.getScaleX();
    }
    @Deprecated
    public double scaleToScreenY(double d)
    {
        return d * affineTransform.getScaleY();
    }
    double getMinX()
    {
        return userBounds.getMinX();
    }

    double getMinY()
    {
        return userBounds.getMinY();
    }

    double getMaxX()
    {
        return userBounds.getMaxX();
    }

    double getMaxY()
    {
        return userBounds.getMaxY();
    }
    
}
