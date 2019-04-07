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
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractShape implements Shape
{
    
    private Rectangle rect;
    protected Rectangle2D bounds;

    protected AbstractShape()
    {
        this(new Rectangle2D.Double());
    }
    protected AbstractShape(Rectangle2D bounds2D)
    {
        this.bounds = bounds2D;
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        return bounds;
    }

    @Override
    public boolean contains(double x, double y)
    {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return bounds.intersects(x, y, w, h);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return bounds.contains(x, y, w, h);
    }

    @Override
    public Rectangle getBounds()
    {
        if (rect == null)
        {
            rect = new Rectangle();
            rect.setRect(bounds);
        }
        return rect;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        return bounds.intersects(r);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return bounds.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    
}
