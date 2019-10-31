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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
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
    protected boolean drawWithControlPoints;

    protected AbstractCubicSpline(Point2D... points)
    {
        this(convert(points));
    }

    protected AbstractCubicSpline(Collection<Point2D> points)
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
    protected double[] createControlPoints(double[] points)
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
    @Override
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
        forceInjection((i)->{});
    }
    public void forceInjection(IntConsumer reporter)
    {
        int length = controlPoints.length-2;
        for (int ii=0;ii<length;ii+=6)
        {
            if (CubicBezierCurves.forceInjection(controlPoints, ii))
            {
                reporter.accept(ii/6);
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
        return eval(x, MoreMath.sqrtEpsilon(x));
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
    protected static double[] convert(Collection<Point2D> points)
    {
        int len = points.size();
        double[] cp = new double[len*2];
        int index = 0;
        for (Point2D point :  points)
        {
            cp[index++] = point.getX();
            cp[index++] = point.getY();
        }
        return cp;
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

    public void setDrawWithControlPoints(boolean drawWithControlPoints)
    {
        this.drawWithControlPoints = drawWithControlPoints;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        if (drawWithControlPoints)
        {
            return new PathIteratorWithControlPoints(at);
        }
        else
        {
            return new PathIteratorImpl(at);
        }
    }

    protected class PathIteratorWithControlPoints extends PathIteratorImpl
    {

        public PathIteratorWithControlPoints(AffineTransform at)
        {
            super(at);
            this.length = controlPoints.length - 1;
        }

        @Override
        public void next()
        {
            index++;
        }
        
    }
    protected class PathIteratorImpl implements PathIterator
    {
        protected AffineTransform at;
        protected int length;
        protected int index;

        public PathIteratorImpl(AffineTransform at)
        {
            this.at = at != null ? at : new AffineTransform();
            this.length = controlPoints.length - 5;
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
            switch (index % 6)
            {
                case 0:
                    index++;
                    break;
                case 1:
                    index+=6;
                    break;
                default:
                    throw new UnsupportedOperationException("should not happen");
            }
        }

        @Override
        public int currentSegment(float[] coords)
        {
            int idx = 6*(index/6);
            switch (index % 6)
            {
                case 0:
                    at.transform(controlPoints, idx, coords, 0, 1);
                    return SEG_MOVETO;
                case 1:
                    at.transform(controlPoints, idx+2, coords, 0, 3);
                    return SEG_CUBICTO;
                case 2:
                    at.transform(controlPoints, idx, coords, 0, 1);
                    return SEG_MOVETO;
                case 3:
                    at.transform(controlPoints, idx+2, coords, 0, 1);
                    return SEG_LINETO;
                case 4:
                    at.transform(controlPoints, idx+4, coords, 0, 1);
                    return SEG_LINETO;
                case 5:
                    at.transform(controlPoints, idx+6, coords, 0, 1);
                    return SEG_LINETO;
                default:
                    throw new UnsupportedOperationException("should not happen");
            }
        }

        @Override
        public int currentSegment(double[] coords)
        {
            int idx = 6*(index/6);
            switch (index % 6)
            {
                case 0:
                    at.transform(controlPoints, idx, coords, 0, 1);
                    return SEG_MOVETO;
                case 1:
                    at.transform(controlPoints, idx+2, coords, 0, 3);
                    return SEG_CUBICTO;
                case 2:
                    at.transform(controlPoints, idx, coords, 0, 1);
                    return SEG_MOVETO;
                case 3:
                    at.transform(controlPoints, idx+2, coords, 0, 1);
                    return SEG_LINETO;
                case 4:
                    at.transform(controlPoints, idx+4, coords, 0, 1);
                    return SEG_LINETO;
                case 5:
                    at.transform(controlPoints, idx+6, coords, 0, 1);
                    return SEG_LINETO;
                default:
                    throw new UnsupportedOperationException("should not happen");
            }
        }
        
    }
}
