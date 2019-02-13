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

import java.awt.Point;

/**
 * LineDrawer class uses Bresenham's line algorithm to plot line points.
 * <p>Note! There are no restrictions on lines direction.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class LineDrawer
{
    /**
     * Draws orthogonal to derivate width length line having center at point.
     * @param point
     * @param derivate
     * @param width
     * @param plot 
     */
    public static void fillWidth(Point point, Point derivate, double width, PlotOperator plot)
    {
        fillWidth(point.x, point.y, derivate.x, derivate.y, width, plot);
    }
    /**
     * Draws orthogonal to derivate width length line having center at (x0, y0).
     * @param x0
     * @param y0
     * @param derivateX
     * @param derivateY
     * @param width
     * @param plot 
     */
    public static void fillWidth(double x0, double y0, double derivateX, double derivateY, double width, PlotOperator plot)
    {
        double halfWidth = width/2.0;
        if (Math.abs(derivateY) <= Double.MIN_VALUE)
        {
            drawLine((int)(x0), (int)(y0+halfWidth), (int)(x0), (int)(y0-halfWidth), plot);
        }
        else
        {
            double s = derivateX/-derivateY;
            double w2 = halfWidth*halfWidth;
            double x1 = Math.sqrt(w2/(s*s+1));
            double y1 = s*x1;
            drawLine((int)(x0+x1), (int)(y0+y1), (int)(x0-x1), (int)(y0-y1), plot);
        }
    }
    /**
     * Draws line (p1, p2) by plotting points using plot
     * @param p1
     * @param p2
     * @param plot 
     * @param color 
     */
    public static void drawLine(Point p1, Point p2, PlotOperator plot)
    {
        drawLine(p1.x, p1.y, p2.x, p2.y, plot);
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
    public static void drawLine(int x0, int y0, int x1, int y1, PlotOperator plot)
    {
        if (x0 > x1)
        {
            drawLine(x1, y1, x0, y0, plot);
        }
        else
        {
            if (y0 > y1)
            {
                drawLine(x0, -y0, x1, -y1, (x, y) -> plot.plot(x, -y));
            }
            else
            {
                drawLine1(x0, y0, x1, y1, plot);    // ok to go ahead
            }
        }
    }

    private static void drawLine1(int x0, int y0, int x1, int y1, PlotOperator plot)
    {
        assert x0 <= x1;
        assert y0 <= y1;
        double deltax = x1 - x0;
        double deltay = y1 - y0;
        if (deltay <= deltax)
        {
            drawLine1(x0, y0, x1, plot, deltay, Math.abs(deltay / deltax));
        }
        else
        {
            drawLine1(y0, x0, y1, (x, y) -> plot.plot(y, x), deltax, Math.abs(deltax / deltay));
        }
    }
    private static void drawLine1(int x0, int y0, int x1, PlotOperator plot, double deltay, double deltaerr)
    {
        double error = 0.0;
        int y = y0;
        double signum = Math.signum(deltay);
        for (int x = x0;x<=x1;x++)
        {
            plot.plot(x, y);
            error += deltaerr;
            if (error >= 0.5)
            {
                y += signum;
                error -= 1.0;
            }
        }
    }
}

