/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import static org.vesalainen.math.BezierCurve.*;
import org.vesalainen.math.BezierOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Drawer
{
    void setFont(Font font);
    default void text(double x, double y, String text)
    {
        text(x, y, TextAlignment.START_X, text);
    }
    void text(double x, double y, TextAlignment alignment, String text);
    void setColor(Color color);
    Color getColor();
    void setLineWidth(double width);
    double getLineWidth();
    void setTransform(DoubleTransform transform, AffineTransform affineTransform);
    void draw(Shape shape);
    // ----------------- to be removed --------------------------------
    @Deprecated default Rectangle2D bounds(String text)
    {
        throw new UnsupportedOperationException();
    }
    /**
     * @deprecated Use setColor
     * @param color 
     */
    default void color(Color color)
    {
        setColor(color);
    }
    /**
     * @deprecated Use setFont
     * @param name
     * @param style
     * @param size 
     */
    default void font(String name, int style, int size)
    {
        setFont(new Font(name, style, size));
    }
    @Deprecated default void circle(double x, double y, double r)
    {
        ellipse(x, y, r, r);
    }
    @Deprecated default void ellipse(double x, double y, double rx, double ry)
    {
        throw new UnsupportedOperationException();
    }
    @Deprecated default void line(double x1, double y1, double x2, double y2)
    {
        throw new UnsupportedOperationException();
    }
    @Deprecated default void polyline(double[] x, double[] y)
    {
        if (x.length != y.length)
        {
            throw new IllegalArgumentException("dimensions differ");
        }
        if (x.length > 1)
        {
            int len = x.length;
            double x1 = x[0];
            double y1 = y[0];
            for (int ii=1;ii<len;ii++)
            {
                double x2 = x[ii];
                double y2 = y[ii];
                line(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
            }
        }
    }
}
