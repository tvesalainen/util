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
import java.awt.Graphics2D;

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
    public void color(Color color)
    {
        g.setColor(color);
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
        switch (alignment)
        {
            case START:
                g.drawString(text, (float)x, (float)y);
                break;
            case MIDDLE:
                g.drawString(text, (float)x-g.getFontMetrics().stringWidth(text)/2, (float)y);
                break;
            case END:
                g.drawString(text, (float)x-g.getFontMetrics().stringWidth(text), (float)y);
                break;
            default:
                throw new UnsupportedOperationException(alignment+" not supported");
        }
    }

}
