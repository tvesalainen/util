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
package org.vesalainen.math;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import static org.vesalainen.math.BezierCurve.CUBIC;
import org.vesalainen.ui.AbstractShape;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractCubicSpline extends AbstractShape implements MathFunction
{
    
    protected boolean isInjection;
    protected double[] xArr;
    protected double[] controlPoints;
    protected ParameterizedOperator[] curves;

    protected AbstractCubicSpline(double... points)
    {
        if (
                points.length % 2 != 0 ||
                points.length < 6
                )
        {
            throw new IllegalArgumentException("wrong number of points");
        }
        this.isInjection = checkInjection(points, 0);
        int n = points.length/2;
        Matrix S = Matrix.getInstance(n, points);
        xArr = new double[n];
        S.getColumn(0, xArr, 0);
        Matrix M = createMatrix(n);
        Matrix B = M.solve(S);
        controlPoints = new double[6*(n-1)+2];
        int pi = 0;
        S.getRow(0, controlPoints, pi);
        pi += 2;
        curves = new ParameterizedOperator[n-1];
        int offset = 0;
        for (int ii=0;ii<curves.length;ii++)
        {
            double bx1 = B.get(ii, 0);
            double by1 = B.get(ii, 1);
            double bx2 = B.get(ii+1, 0);
            double by2 = B.get(ii+1, 1);
            controlPoints[pi++] = bx1+(bx2-bx1)/3;
            controlPoints[pi++] = by1+(by2-by1)/3;
            controlPoints[pi++] = bx1+2*(bx2-bx1)/3;
            controlPoints[pi++] = by1+2*(by2-by1)/3;
            S.getRow(ii+1, controlPoints, pi);
            pi += 2;
            curves[ii] = CUBIC.operator(controlPoints, offset);
            offset += 6;
        }
        int len = controlPoints.length/2;
        for (int ii=0;ii<len;ii+=2)
        {
            bounds.add(controlPoints[2*ii], controlPoints[2*ii+1]);
        }
    }

    protected abstract Matrix createMatrix(int n);
    
    @Override
    public double applyAsDouble(double x)
    {
        return eval(x, 10 * Math.ulp(x));
    }

    public double eval(double x, double deltaX)
    {
        return eval(x, deltaX, new Point2D.Double());
    }

    public double eval(double x, double deltaX, Point2D.Double pnt)
    {
        if (!isInjection)
        {
            throw new IllegalArgumentException("curve is not injection");
        }
        int idx = Arrays.binarySearch(xArr, x);
        if (idx >= 0)
        {
            return controlPoints[6 * idx + 1];
        }
        else
        {
            int ip = -idx - 1;
            if (ip <= 0 || ip > curves.length)
            {
                throw new IllegalArgumentException("out of range");
            }
            ParameterizedOperator curve = curves[ip - 1];
            double s1 = xArr[ip - 1];
            double s2 = xArr[ip];
            double t = (x - s1) / (s2 - s1);
            curve.eval(t, pnt::setLocation);
            int count = 0;
            while (Math.abs(x - pnt.x) > deltaX)
            {
                if (count > 128)
                {
                    throw new IllegalArgumentException("deltaX too small");
                }
                double d = x - pnt.x;
                curve.derivative().eval(t, pnt::setLocation);
                t += d / pnt.x;
                curve.eval(t, pnt::setLocation);
                count++;
            }
            return pnt.y;
        }
    }

    public boolean isIsInjection()
    {
        return isInjection;
    }

    protected boolean checkInjection(double[] points, int offset)
    {
        double v = points[offset];
        for (int ii = 2; ii < points.length; ii += 2)
        {
            if (v >= points[ii + offset])
            {
                return false;
            }
            v = points[ii + offset];
        }
        return true;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PathIteratorImpl(at);
    }
    private class PathIteratorImpl implements PathIterator
    {
        private AffineTransform at;
        private int index;

        public PathIteratorImpl(AffineTransform at)
        {
            this.at = at != null ? at : new AffineTransform();
        }
        
        @Override
        public int getWindingRule()
        {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone()
        {
            return index >= controlPoints.length;
        }

        @Override
        public void next()
        {
            if (index == 0)
            {
                index += 2;
            }
            else
            {
                index += 6;
            }
        }

        @Override
        public int currentSegment(float[] coords)
        {
            if (index == 0)
            {
                at.transform(controlPoints, index, coords, 0, 1);
                return SEG_MOVETO;
            }
            else
            {
                at.transform(controlPoints, index, coords, 0, 3);
                return SEG_CUBICTO;
            }
        }

        @Override
        public int currentSegment(double[] coords)
        {
            if (index == 0)
            {
                at.transform(controlPoints, index, coords, 0, 1);
                return SEG_MOVETO;
            }
            else
            {
                at.transform(controlPoints, index, coords, 0, 3);
                return SEG_CUBICTO;
            }
        }
        
    }
}
