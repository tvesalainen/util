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

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;

/**
 * <p>This class is NOT thread-safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractDrawer implements Drawer
{
    protected Font font;
    protected Color color;
    protected double lineWidth=1.0;
    protected DoubleTransformer transform;
    protected Point2D.Double tmp = new Point2D.Double();
    
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
    public double getLineWidth()
    {
        return lineWidth;
    }

    @Override
    public void setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
    }

    @Override
    public void setTransform(DoubleTransformer transform)
    {
        this.transform = transform;
    }

    @Override
    public DoubleTransformer getTransform()
    {
        return transform;
    }

}
