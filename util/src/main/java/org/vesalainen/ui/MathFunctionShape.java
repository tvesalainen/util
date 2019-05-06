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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import org.vesalainen.math.MathFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MathFunctionShape extends AbstractShape
{
    private MathFunction f;
    private double delta;

    public MathFunctionShape(MathFunction f, Rectangle2D bounds, int xResolution)
    {
        this(f, bounds, 3*bounds.getWidth()/xResolution);
    }
    public MathFunctionShape(MathFunction f, Rectangle2D bounds, double delta)
    {
        super(bounds);
        this.f = f;
        this.delta = delta;
    }
    

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PathIteratorImpl(at);
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
            this.x = bounds.getX();
            this.max = bounds.getMaxX();
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
            if (x < max)
            {
                y = f.applyAsDouble(x);
                if (!bounds.contains(x, y))
                {
                    closed = true;
                    x += delta;
                    while (x < max)
                    {
                        y = f.applyAsDouble(x);
                        if (bounds.contains(x, y))
                        {
                            return;
                        }
                        x += delta;
                    }
                }
            }
        }
    }
}
