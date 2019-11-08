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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.vesalainen.math.BasicPolygon;
import org.vesalainen.math.matrix.DoubleMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoublePolygon implements Shape
{
    private Path2D.Double path = new Path2D.Double();
    private boolean movedTo;

    public DoublePolygon()
    {
    }
    
    public DoublePolygon(BasicPolygon polygon)
    {
        this(polygon.points);
    }    

    public DoublePolygon(DoubleMatrix points)
    {
        this(points.data(), points.rows());
    }
    public DoublePolygon(double[] d)
    {
        this(d, d.length);
    }
    public DoublePolygon(double[] d, int len)
    {
        add(d, len);
    }
    
    public final void add(double[] d, int pnts)
    {
        for (int ii=0;ii<pnts;ii++)
        {
            add(d[2*ii], d[2*ii+1]);
        }
    }
    public final void add(double x, double y)
    {
        if (!movedTo)
        {
            path.moveTo(x, y);
            movedTo = true;
        }
        else
        {
            path.lineTo(x, y);
        }
    }

    public final synchronized void closePath()
    {
        path.closePath();
    }

    @Override
    public final Rectangle getBounds()
    {
        return path.getBounds();
    }

    @Override
    public final boolean contains(double x, double y)
    {
        return path.contains(x, y);
    }

    @Override
    public final boolean contains(Point2D p)
    {
        return path.contains(p);
    }

    @Override
    public final boolean contains(double x, double y, double w, double h)
    {
        return path.contains(x, y, w, h);
    }

    @Override
    public final boolean contains(Rectangle2D r)
    {
        return path.contains(r);
    }

    @Override
    public final boolean intersects(double x, double y, double w, double h)
    {
        return path.intersects(x, y, w, h);
    }

    @Override
    public final boolean intersects(Rectangle2D r)
    {
        return path.intersects(r);
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        return path.getBounds2D();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return path.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return path.getPathIterator(at, flatness);
    }
}
