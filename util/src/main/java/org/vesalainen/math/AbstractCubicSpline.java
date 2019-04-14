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

import org.vesalainen.math.matrix.DoubleMatrix;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.vesalainen.math.BezierCurve.CUBIC;
import org.vesalainen.ui.AbstractShape;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractCubicSpline extends AbstractShape implements MathFunction
{
    private static final Map<Class<? extends AbstractCubicSpline>,Map<Integer,DoubleMatrix>> matrixCache = new HashMap<>();
    
    protected boolean closed;
    protected boolean injection;
    protected double[] xArr;
    protected double[] controlPoints;
    protected ParameterizedOperator[] curves;
    protected int pointCount;
    protected int curveCount;

    protected AbstractCubicSpline()
    {
    }

    protected AbstractCubicSpline(double... points)
    {
        this(false, points);
    }
    protected AbstractCubicSpline(boolean closed, double... points)
    {
        this.closed = closed;
        update(points);
    }
    /**
     * Updates points
     * @param points 
     */
    public final void update(double... points)
    {
        checkInput(points);
        if (!closed)
        {
            this.injection = checkInjection(points, 0);
        }
        double[] cp = createControlPoints(points);
        init(cp);
    }
    protected final void checkInput(double[] points)
    {
        if (
                points.length % 2 != 0 ||
                points.length < 6
                )
        {
            throw new IllegalArgumentException("wrong number of points");
        }
    }
    protected final double[] createControlPoints(double[] points)
    {
        int n = points.length/2;
        DoubleMatrix S = DoubleMatrix.getInstance(n, points);
        int cc;
        if (closed)
        {
            cc = n;
        }
        else
        {
            cc = n-1;
        }
        DoubleMatrix M = getMatrix(n);
        DoubleMatrix B = M.solve(S);
        double[] cp = new double[6*cc+2];
        int pi = 0;
        S.getRow(0, cp, pi);
        pi += 2;
        for (int ii=0;ii<cc;ii++)
        {
            int ni = (ii+1)%n;
            double bx1 = B.get(ii, 0);
            double by1 = B.get(ii, 1);
            double bx2 = B.get(ni, 0);
            double by2 = B.get(ni, 1);
            cp[pi++] = bx1+(bx2-bx1)/3;
            cp[pi++] = by1+(by2-by1)/3;
            cp[pi++] = bx1+2*(bx2-bx1)/3;
            cp[pi++] = by1+2*(by2-by1)/3;
            S.getRow(ni, cp, pi);
            pi += 2;
        }
        return cp;
    }
    protected final void init(double[] controlPoints)
    {
        this.controlPoints = controlPoints;
        this.curveCount = (controlPoints.length-2)/6;
        if (closed)
        {
            pointCount = curveCount;
        }
        else
        {
            pointCount = curveCount+1;
        }
        if (injection)
        {
            xArr = new double[pointCount];
            for (int ii=0;ii<pointCount;ii++)
            {
                xArr[ii] = controlPoints[6*ii];
            }
        }
        curves = new ParameterizedOperator[curveCount];
        int offset = 0;
        for (int ii=0;ii<curveCount;ii++)
        {
            curves[ii] = CUBIC.operator(controlPoints, offset);
            offset += 6;
        }
        int len = controlPoints.length/2;
        for (int ii=0;ii<len;ii++)
        {
            bounds.add(controlPoints[2*ii], controlPoints[2*ii+1]);
        }
    }
    /**
     * Returns class cached DoubleMatrix with decompose already called.
     * @param n
     * @return 
     */
    protected DoubleMatrix getMatrix(int n)
    {
        Map<Integer, DoubleMatrix> degreeMap = matrixCache.get(this.getClass());
        if (degreeMap == null)
        {
            degreeMap = new HashMap<>();
            matrixCache.put(this.getClass(), degreeMap);
        }
        DoubleMatrix m = degreeMap.get(n);
        if (m == null)
        {
            m = createMatrix(n);
            m.decompose();
            degreeMap.put(n, m);
        }
        return m;
    }
    protected abstract DoubleMatrix createMatrix(int n);
    
    @Override
    public double applyAsDouble(double x)
    {
        return eval(x, 10 * Math.ulp(x));
    }

    public ParameterizedOperator getCurve(double x)
    {
        int idx = Arrays.binarySearch(xArr, x);
        if (idx >= 0)
        {
            if (idx != xArr.length-1)
            {
                return curves[idx];
            }
            else
            {
                return curves[idx-1];
            }
        }
        else
        {
            int ip = -idx - 1;
            if (ip <= 0 || ip > curves.length)
            {
                throw new IllegalArgumentException("out of range");
            }
            return curves[ip - 1];
        }
    }
    public double eval(double x, double deltaX)
    {
        return eval(x, deltaX, new Point2D.Double());
    }
    public double eval(double x, double deltaX, Point2D.Double pnt)
    {
        if (!injection)
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
            return curve.evalY(x, deltaX, pnt);
        }
    }

    public boolean isIsInjection()
    {
        return injection;
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
