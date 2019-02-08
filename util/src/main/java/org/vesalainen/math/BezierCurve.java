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

import java.awt.geom.Point2D;
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 * BezierCurve calculates n-degree Bezier curve
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="bb_bezier.pdf">http://www.math.ucla.edu/~baker/149.1.02w/handouts/bb_bezier.pdf</a>
 */
public class BezierCurve
{
    /**
     * 2-degree
     */
    public static final BezierCurve QUAD = new BezierCurve(2);
    /**
     * 3-degree
     */
    public static final BezierCurve CUBIC = new BezierCurve(3);
    private DoubleUnaryOperator[] array;
    private int length;
    /**
     * Creates Bezier curve of degree
     * @param degree 
     */
    public BezierCurve(int degree)
    {
        if (degree < 1)
        {
            throw new IllegalArgumentException(degree+" not allowed");
        }
        this.length = degree+1;
        this.array = new DoubleUnaryOperator[length];
        for (int n=0;n<=degree;n++)
        {
            array[n] = BernsteinPolynomial.b(degree, n);
        }
    }
    /**
     * Returns P(t) in curve
     * @param t
     * @param controlPoints
     * @return 
     */
    public Point2D.Double calc(double t, Point2D.Double... controlPoints)
    {
        if (controlPoints.length != length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        double[] cp = convert(controlPoints);
        Point2D.Double point = new Point2D.Double();
        calc(t, point::setLocation, cp);
        return point;
    }
    /**
     * Calculates P(t) 
     * @param t
     * @param result
     * @param controlPoints 
     */
    public void calc(double t, double[] result, double... controlPoints)
    {
        if (result.length < 2)
        {
            throw new IllegalArgumentException("result needs size 2");
        }
        calc(t, (x,y)->{result[0]=x;result[1]=y;}, controlPoints);
    }
    /**
     * Calculates P(t) 
     * @param t
     * @param result
     * @param controlPoints 
     */
    public void calc(double t, DoubleBiConsumer result, double... controlPoints)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t not in [0..1]");
        }
        if (controlPoints.length < 2*length)
        {
            throw new IllegalArgumentException("control-points length not "+2*length);
        }
        double x = 0;
        double y = 0;
        for (int n=0;n<length;n++)
        {
            double c = array[n].applyAsDouble(t);
            x += controlPoints[2*n]*c;
            y += controlPoints[2*n+1]*c;
        }
        result.accept(x, y);
    }
    /**
     * Calculates path length
     * @param delta
     * @param controlPoints
     * @return 
     */
    public double pathLength(double delta, Point2D.Double... controlPoints)
    {
        if (controlPoints.length != length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        double[] cp = convert(controlPoints);
        return pathLength(delta, cp);
    }
    /**
     * Calculates path length
     * @param delta
     * @param controlPoints
     * @return 
     */
    public double pathLength(double delta, double... controlPoints)
    {
        if (delta < 0 || delta > 0.5)
        {
            throw new IllegalArgumentException("illegal delta");
        }
        Point2D.Double prev = new Point2D.Double();
        Point2D.Double next = new Point2D.Double();
        double sum = 0;
        calc(0, prev::setLocation, controlPoints);
        for (double t=delta;t<=1.0;t+=delta)
        {
            calc(t, next::setLocation, controlPoints);
            sum += prev.distance(next);
            prev.setLocation(next);;
        }
        return sum;
    }
    /**
     * Estimates path length
     * @param controlPoints
     * @return 
     */
    public double pathLengthEstimate(Point2D.Double... controlPoints)
    {
        if (controlPoints.length != length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        double[] cp = convert(controlPoints);
        return pathLengthEstimate(cp);
    }
    /**
     * Estimates path length
     * @param controlPoints
     * @return 
     */
    public double pathLengthEstimate(double... controlPoints)
    {
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        for (int n=0;n<length;n++)
        {
            maxX = Math.max(maxX, controlPoints[2*n]);
            maxY = Math.max(maxY, controlPoints[2*n+1]);
            minX = Math.min(minX, controlPoints[2*n]);
            minY = Math.min(minY, controlPoints[2*n+1]);
        }
        return (maxX-minX+maxY-minY)*1.35;
    }
    private static double[] convert(Point2D.Double... points)
    {
        int len = points.length;
        double[] cp = new double[len*2];
        for (int n=0;n<len;n++)
        {
            cp[2*n] = points[n].x;
            cp[2*n+1] = points[n].y;
        }
        return cp;
    }
}
