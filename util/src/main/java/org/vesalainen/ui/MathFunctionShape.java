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
import org.vesalainen.math.MathFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MathFunctionShape implements Shape
{
    private MathFunction f;
    private Rectangle bounds;
    private Rectangle2D bounds2D;
    private double delta;

    public MathFunctionShape(MathFunction f, Rectangle2D bounds, int xResolution)
    {
        this(f, bounds, 3*bounds.getWidth()/xResolution);
    }
    public MathFunctionShape(MathFunction f, Rectangle2D bounds, double delta)
    {
        this.f = f;
        this.bounds2D = bounds;
        this.delta = delta;
    }
    
    @Override
    public Rectangle2D getBounds2D()
    {
        return bounds2D;
    }

    @Override
    public boolean contains(double x, double y)
    {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return bounds2D.intersects(x, y, w, h);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return bounds2D.contains(x, y, w, h);
    }

    @Override
    public Rectangle getBounds()
    {
        if (bounds == null)
        {
            bounds = new Rectangle();
            bounds.setRect(bounds2D);
        }
        return bounds;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        return bounds2D.intersects(r);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return bounds2D.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PathIteratorImpl(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    private class PathIteratorImpl implements PathIterator
    {
        private AffineTransform at;
        private double x;
        private double y;
        private double max;
        private boolean closed = true;

        public PathIteratorImpl(AffineTransform at)
        {
            this.at = at;
            this.x = bounds2D.getX();
            this.max = bounds2D.getMaxX();
            eval();
        }
        
        @Override
        public int getWindingRule()
        {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone()
        {
            return x >= max;
        }

        @Override
        public void next()
        {
            x += delta;
            eval();
        }

        @Override
        public int currentSegment(float[] coords)
        {
            coords[0] = (float) x;
            coords[1] = (float) y;
            if (at != null)
            {
                at.transform(coords, 0, coords, 0, 2);
            }
            if (closed)
            {
                closed = false;
                return SEG_MOVETO;
            }
            else
            {
                return SEG_LINETO;
            }
        }

        @Override
        public int currentSegment(double[] coords)
        {
            coords[0] = x;
            coords[1] = y;
            if (at != null)
            {
                at.transform(coords, 0, coords, 0, 2);
            }
            if (closed)
            {
                closed = false;
                return SEG_MOVETO;
            }
            else
            {
                return SEG_LINETO;
            }
        }
        
        private void eval()
        {
            y = f.applyAsDouble(x);
            while (x < max && !bounds2D.contains(x, y))
            {
                x += delta;
                y = f.applyAsDouble(x);
                closed = true;
            }
        }
    }
}
