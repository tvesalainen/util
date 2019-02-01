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
import java.awt.Point;

/**
 * LineDrawer class uses Bresenham's line algorithm to plot line points.
 * <p>Note! There are no restrictions on lines direction.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class LineDrawer
{
    /**
     * Draws line (p1, p2) by plotting points using plot
     * @param p1
     * @param p2
     * @param plot 
     * @param color 
     */
    public static void drawLine(Point p1, Point p2, PlotOperator plot, Color color)
    {
        drawLine(p1.x, p1.y, p2.x, p2.y, plot, color);
    }
    /**
     * Draws line ((x0, y0), (x1, y1)) by plotting points using plot
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param plot 
     * @param color 
     */
    public static void drawLine(int x0, int y0, int x1, int y1, PlotOperator plot, Color color)
    {
        if (x0 > x1)
        {
            drawLine(x1, y1, x0, y0, plot, color);
        }
        else
        {
            if (y0 > y1)
            {
                drawLine(x0, -y0, x1, -y1, (x, y, c) -> plot.plot(x, -y, c), color);
            }
            else
            {
                drawLine1(x0, y0, x1, y1, plot, color);    // ok to go ahead
            }
        }
    }

    private static void drawLine1(int x0, int y0, int x1, int y1, PlotOperator plot, Color color)
    {
        assert x0 <= x1;
        assert y0 <= y1;
        double deltax = x1 - x0;
        double deltay = y1 - y0;
        if (deltay <= deltax)
        {
            drawLine1(x0, y0, x1, plot, deltay, Math.abs(deltay / deltax), color);
        }
        else
        {
            drawLine1(y0, x0, y1, (x, y, c) -> plot.plot(y, x, c), deltax, Math.abs(deltax / deltay), color);
        }
    }
    private static void drawLine1(int x0, int y0, int x1, PlotOperator plot, double deltay, double deltaerr, Color color)
    {
        double error = 0.0;
        int y = y0;
        double signum = Math.signum(deltay);
        for (int x = x0;x<=x1;x++)
        {
            plot.plot(x, y, color);
            error += deltaerr;
            if (error >= 0.5)
            {
                y += signum;
                error -= 1.0;
            }
        }
    }
}

