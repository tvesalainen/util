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
import org.vesalainen.util.ArrayHelp;

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

    protected AbstractCubicSpline(Point2D... points)
    {
        this(convert(points));
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
    private void update(double... points)
    {
        checkInput(points);
        if (!closed)
        {   // check first with initialpoints
            this.injection = ArrayHelp.arePointsInXOrder(points);
        }
        double[] cp = createControlPoints(points);
        init(cp);
        if (!closed && injection)
        {   // might still not be injection if control points cause loops etc.
            this.injection = ArrayHelp.arePointsInXOrder(cp);
        }
    }
    /**
     * Returns true if each x-value evaluates to exactly one y-value.
     * @return 
     */
    public boolean isInjection()
    {
        return injection;
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
    }
    protected void calculateBounds()
    {
        int len = controlPoints.length/2;
        for (int ii=0;ii<len;ii++)
        {
            bounds.add(controlPoints[2*ii], controlPoints[2*ii+1]);
        }
    }

    /**
    * Modifies PolarCubicSpline to be x-injection
    * after which it is not PolarCubicSpline any more.
    * 
    * <p>It can be proved that Bezier curve is x-injection if it's control points
    * x-components are in ascending order. I.e x0 &le; x1 &le; x2 &le; x3. If Bezier
    * curve's x1 &gt; x2  it can be modified to be x-injection by changing P1 and P2.
    * After modification first and second derivatives don't equal at connecting
    * points any more.
     */
    public void forceInjection()
    {
        int length = controlPoints.length-2;
        for (int ii=0;ii<length;ii+=6)
        {
            double x0 = controlPoints[ii];
            double x1 = controlPoints[ii+2];
            double x2 = controlPoints[ii+4];
            double x3 = controlPoints[ii+6];
            if (x0 > x3)
            {
                throw new IllegalArgumentException("x0 > x3");
            }
            if (!(x0 <= x1 && x1 <= x2 && x2 <= x3))
            {
                double a = (x0+x3)/2.0;
                controlPoints[ii+2] = a;
                controlPoints[ii+4] = a;
            }
        }
        injection = true;
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
        return eval(x, 100 * Math.ulp(x));
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
            return curve.evalY(x, deltaX);
        }
    }
    protected static double[] convert(Point2D... points)
    {
        int len = points.length;
        double[] cp = new double[len*2];
        for (int n=0;n<len;n++)
        {
            cp[2*n] = points[n].getX();
            cp[2*n+1] = points[n].getY();
        }
        return cp;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PathIteratorImpl(at);
    }

    protected class PathIteratorImpl implements PathIterator
    {
        private AffineTransform at;
        private int offset;
        private int length;
        private int index;

        public PathIteratorImpl(AffineTransform at)
        {
            this(at, 0, controlPoints.length);
        }
        public PathIteratorImpl(AffineTransform at, int offset, int length)
        {
            this.at = at != null ? at : new AffineTransform();
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public int getWindingRule()
        {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone()
        {
            return index >= length;
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
                at.transform(controlPoints, offset+index, coords, 0, 1);
                return SEG_MOVETO;
            }
            else
            {
                at.transform(controlPoints, offset+index, coords, 0, 3);
                return SEG_CUBICTO;
            }
        }

        @Override
        public int currentSegment(double[] coords)
        {
            if (index == 0)
            {
                at.transform(controlPoints, offset+index, coords, 0, 1);
                return SEG_MOVETO;
            }
            else
            {
                at.transform(controlPoints, offset+index, coords, 0, 3);
                return SEG_CUBICTO;
            }
        }
        
    }
}
