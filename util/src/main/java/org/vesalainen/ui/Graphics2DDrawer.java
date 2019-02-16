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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.function.IntBinaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Graphics2DDrawer implements Drawer
{
    private Graphics2D g;

    public Graphics2DDrawer(Graphics2D g)
    {
        this.g = g;
    }

    @Override
    public void setFont(Font font)
    {
        g.setFont(font);
    }
    
    @Override
    public void setColor(Color color)
    {
        g.setColor(color);
    }

    @Override
    public Color getColor()
    {
        return g.getColor();
    }

    @Override
    public void setTransform(DoubleTransform t, AffineTransform transform)
    {
        AffineTransform safe = g.getTransform();
        g.transform(transform);
        g.setTransform(safe);
    }

    @Override
    public void draw(Shape shape)
    {
        g.draw(shape);
    }

    @Override
    public void setPaint(Paint paint)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPattern(IntBinaryOperator pattern)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStroke(BasicStroke stroke)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fill(Shape shape)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawLine(double... cp)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawQuad(double... cp)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawCubic(double... cp)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closePath(double... cp)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void circle(double x, double y, double r)
    {
        g.drawOval((int)x, (int)y, (int)r*2, (int)r*2);
    }

    @Override
    public void ellipse(double x, double y, double rx, double ry)
    {
        g.drawOval((int)x, (int)y, (int)rx*2, (int)ry*2);
    }

    @Override
    public void line(double x1, double y1, double x2, double y2)
    {
        g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
    }

    @Override
    public void font(String name, int style, int size)
    {
        g.setFont(new Font(name, style, size));
    }

    @Override
    public void text(double x, double y, TextAlignment alignment, String text)
    {
        FontMetrics fm = g.getFontMetrics();
        switch (alignment)
        {
            case START_X:
            case START_Y:
                g.drawString(text, (float)x, (float)y-fm.getMaxDescent());
                break;
            case MIDDLE_X:
                g.drawString(text, (float)x-fm.stringWidth(text)/2, (float)y-fm.getMaxDescent());
                break;
            case END_X:
                g.drawString(text, (float)x-fm.stringWidth(text), (float)y-fm.getMaxDescent());
                break;
            case MIDDLE_Y:
                g.drawString(text, (float)x, (float)y-fm.getMaxDescent()+fm.getHeight()/2);
                break;
            case END_Y:
                g.drawString(text, (float)x, (float)y-fm.getMaxDescent()+fm.getHeight());
                break;
            default:
                throw new UnsupportedOperationException(alignment+" not supported");
        }
    }

    @Override
    public Rectangle2D bounds(String text)
    {
        return g.getFontMetrics().getStringBounds(text, g);
    }

}
