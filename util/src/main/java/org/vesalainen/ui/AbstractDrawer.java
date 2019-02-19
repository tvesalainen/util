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

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
    protected DoubleTransform[] derivates;
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
    public void setTransform(DoubleTransform transform, DoubleTransform inverse, DoubleTransform[] derivates, double scale)
    {
        this.transform = transform;
        this.derivates = derivates;
        this.inverse = inverse;
        this.scale = scale;
    }

    @Override
    public void userToScreen(double x, double y, DoubleBiConsumer screen)
    {
        transform.transform(x, y, screen);
    }

    @Override
    public void screenToUser(double x, double y, DoubleBiConsumer user)
    {
        inverse.transform(x, y, user);
    }
    
    protected void updateDelta(double dx, double dy)
    {
        deltax = dx;
        deltay = dy;
        for (DoubleTransform d : derivates)
        {
            d.transform(dx, dy, this::multiplyDerivate);
        }
        delta = Math.hypot(deltax, deltay);
    }
    private void multiplyDerivate(double dx, double dy)
    {
        deltax *= dx;
        deltay *= dy;
    }
}
