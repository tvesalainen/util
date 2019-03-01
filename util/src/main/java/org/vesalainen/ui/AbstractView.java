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
import static java.awt.geom.Rectangle2D.*;
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
    protected DoubleBounds transformedUserBounds = new DoubleBounds();
    protected boolean keepAspectRatio;
    protected DoubleTransform transform = DoubleTransform.identity();
    protected DoubleTransform combinedTransform;
    protected AffineTransform affineTransform = new AffineTransform();
    protected double scale;
    protected DoubleTransform[] derivates;
    protected DoubleTransform inverse;
    protected DoubleTransform affineDoubleTransform;
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
    public void update(Stream<Shape> shapes)
    {
        if (screenBounds.isEmpty())
        {
            throw new IllegalStateException("not initialized");
        }
        userBounds.clear();
        transformedUserBounds.clear();
        update(minUserBounds);
        shapes.map(Shape::getBounds2D).forEach(this::update);
    }
    public void calculate()
    {
        Transforms.createScreenTransform(transformedUserBounds, screenBounds, keepAspectRatio, affineTransform);
        affineDoubleTransform = Transforms.affineTransform(affineTransform);
        combinedTransform = transform.andThen(affineDoubleTransform);
        derivates = new DoubleTransform[]{transform.derivate(), affineDoubleTransform.derivate()};
        inverse = affineDoubleTransform.inverse().andThen(transform.inverse());
        scale = (Math.abs(affineTransform.getScaleX())+Math.abs(affineTransform.getScaleY()))/2.0;
    }
    protected void update(Rectangle2D bounds)
    {
        if (!bounds.isEmpty())
        {
            updatePoint(bounds.getMaxX(), bounds.getMaxY());
            updatePoint(bounds.getMinX(), bounds.getMinY());
        }
    }
    /**
     * Updates the limits if point is not inside visible screen.
     * @param x
     * @param y 
     */
    public void updatePoint(double x, double y)
    {
        userBounds.add(x, y);
        transform.transform(x, y, transformedUserBounds::add);
    }
    /**
     * Enlarges margin in screen coordinates to given directions
     * @param bounds
     * @param dirs 
     */
    public void setMargin(Rectangle2D bounds, Direction... dirs)
    {
        for (Direction dir : dirs)
        {
            switch (dir)
            {
                case BOTTOM:
                    inverse.transform(0, screenBounds.height+bounds.getHeight(), this::updatePoint);
                    break;
                case LEFT:
                    inverse.transform(screenBounds.x-bounds.getWidth(), 0 , this::updatePoint);
                    break;
                case RIGHT:
                    inverse.transform(screenBounds.width+bounds.getWidth(), 0 , this::updatePoint);
                    break;
                case TOP:
                    inverse.transform(0, screenBounds.y-bounds.getHeight(), this::updatePoint);
                    break;
            }
        }
    }
    /**
     * @deprecated 
     * Adds margin to visible screen. 
     * @param m Margin m*width/height
     */
    public void margin(double m)
    {
        double xMin = transformedUserBounds.getMinX();
        double yMin = transformedUserBounds.getMinY();
        double xMax = transformedUserBounds.getMaxX();
        double yMax = transformedUserBounds.getMaxY();
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
     * Translates cartesian x-coordinate to screen coordinate.
     * @param x
     * @return 
     */
    public double toScreenX(double x)
    {
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(x, 0);
        combinedTransform.transform(src, dst);
        return dst.getX();
        //return scaleX * x + xOff;
    }
    /**
     * Translates cartesian y-coordinate to screen coordinate.
     * @param y
     * @return 
     */
    public double toScreenY(double y)
    {
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(0, y);
        combinedTransform.transform(src, dst);
        return dst.getY();
    }
    /**
     * Translates screen x-coordinate to cartesian coordinate.
     * @param x
     * @return 
     */
    public double fromScreenX(double x)
    {
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(x, 0);
        inverse.transform(src, dst);
        return dst.getX();
    }
    /**
     * Translates screen y-coordinate to cartesian coordinate.
     * @param y
     * @return 
     */
    public double fromScreenY(double y)
    {
        Point2D src = srcPnt.get();
        Point2D dst = dstPnt.get();
        src.setLocation(0, y);
        inverse.transform(src, dst);
        return dst.getY();
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
        return transformedUserBounds.getMinX();
    }

    double getMinY()
    {
        return transformedUserBounds.getMinY();
    }

    double getMaxX()
    {
        return transformedUserBounds.getMaxX();
    }

    double getMaxY()
    {
        return transformedUserBounds.getMaxY();
    }
    
}
