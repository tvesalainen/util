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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoublePoint implements Shape
{
    private double[] points;
    private Rectangle2D bounds;

    public DoublePoint(double x, double y)
    {
        this.points = new double[]{x,y};
        this.bounds = new Rectangle2D.Double(x, y, 0, 0);
    }

    @Override
    public Rectangle getBounds()
    {
        return bounds.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        return bounds.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y)
    {
        return bounds.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p)
    {
        return bounds.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return bounds.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        return bounds.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return bounds.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return bounds.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PathIter(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return new PathIter(at);
    }
    
    public class PathIter implements PathIterator
    {
        private double[] pnts;
        private int count;

        public PathIter(AffineTransform at)
        {
            if (at != null)
            {
                this.pnts = new double[2];
                at.transform(points, 0, pnts, 0, 1);
            }
            else
            {
                pnts = points;
            }
        }
        
        @Override
        public int getWindingRule()
        {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone()
        {
            return count >= 2;
        }

        @Override
        public void next()
        {
            count++;
        }

        @Override
        public int currentSegment(float[] coords)
        {
            switch (count)
            {
                case 0:
                    coords[0] = (float) Math.rint(pnts[0]);
                    coords[1] = (float) Math.rint(pnts[1]);
                    return SEG_MOVETO;
                case 1:
                    coords[0] = (float) (Math.rint(pnts[0])+1);
                    coords[1] = (float) (Math.rint(pnts[1])+1);
                    return SEG_LINETO;
            }
            return 0;
        }

        @Override
        public int currentSegment(double[] coords)
        {
            switch (count)
            {
                case 0:
                    coords[0] = Math.rint(pnts[0]);
                    coords[1] = Math.rint(pnts[1]);
                    return SEG_MOVETO;
                case 1:
                    coords[0] = Math.rint(pnts[0])+1;
                    coords[1] = Math.rint(pnts[1])+1;
                    return SEG_LINETO;
            }
            return 0;
        }
        
    }
}
