/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.math.DoubleTransform;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.IntBinaryOperator;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 * <p>This class is NOT thread-safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractDrawer implements Drawer
{
    protected Font font;
    protected Color color;
    protected Paint paint;
    protected IntBinaryOperator pattern;
    protected BasicStroke stroke;
    protected DoubleTransform transform;
    protected DoubleTransform inverse;
    protected Point2D.Double tmp = new Point2D.Double();
    protected DoubleTransform derivative;
    protected Bounds fillBounds = new Bounds();
    protected double scale;
    protected double delta;
    private double deltax;
    private double deltay;
    
    @Override
    public void setFont(Font font)
    {
        this.font = font;
    }

    @Override
    public void text(double x, double y, TextAlignment alignment, String text)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setColor(Color color)
    {
        this.color = color;
    }

    @Override
    public Color getColor()
    {
        return color;
    }

    @Override
    public void setPaint(Paint paint)
    {
        this.paint = paint;
    }

    @Override
    public void setPattern(IntBinaryOperator pattern)
    {
        this.pattern = pattern;
    }

    @Override
    public void setStroke(BasicStroke stroke)
    {
        this.stroke = stroke;
    }

    @Override
    public void setTransform(DoubleTransform transform, double scale)
    {
        this.transform = transform;
        this.derivative = transform.derivative();
        this.inverse = transform.inverse();
        this.scale = scale;
    }

    @Override
    public void drawMark(Shape mark)
    {
        Rectangle2D b = mark.getBounds2D();
        Point2D p1 = new Point2D.Double();
        Point2D p2 = new Point2D.Double();
        transform.transform(b.getCenterX(), b.getCenterY(), p1::setLocation);
        transform.transform(b.getCenterX()+b.getWidth(), b.getCenterY()+b.getHeight(), p2::setLocation);
        double distance = p1.distance(p2)/1.4;
        float lineWidth = stroke.getLineWidth();
        double s = lineWidth/distance;
        fill(Shapes.scaleInPlace(mark, s, s));
    }

    protected void updateDelta(double dx, double dy)
    {
        delta = Math.hypot(dx, dy);
    }
}
