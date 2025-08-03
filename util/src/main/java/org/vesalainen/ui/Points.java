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

import java.awt.geom.Point2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Points
{
    public static double len(Point2D.Double p)
    {
        return Math.hypot(p.x, p.y);
    }
    public static Point2D.Double mul(double factor, Point2D.Double p)
    {
        return new Point2D.Double(factor*p.x, factor*p.y);
    }
    public static Point2D.Double mul(Point2D.Double p1, Point2D.Double p2)
    {
        return new Point2D.Double(p1.x*p2.x, p1.y*p2.y);
    }
    public static Point2D.Double sub(Point2D.Double p1, Point2D.Double p2)
    {
        return add(p1, mul(-1, p2));
    }
    public static Point2D.Double add(Point2D.Double... pts)
    {
        double x = 0;
        double y = 0;
        for (Point2D.Double p : pts)
        {
            x+=p.x;
            y+=p.y;
        }
        return new Point2D.Double(x, y);
    }
}
