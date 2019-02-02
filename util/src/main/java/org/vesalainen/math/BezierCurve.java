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
        double[] result = new double[2];
        double[] cp = new double[controlPoints.length*2];
        for (int n=0;n<length;n++)
        {
            cp[2*n] = controlPoints[n].x;
            cp[2*n+1] = controlPoints[n].y;
        }
        calc(t, result, cp);
        return new Point2D.Double(result[0], result[1]);
    }
    /**
     * Calculates P(t) 
     * @param t
     * @param result
     * @param controlPoints 
     */
    public void calc(double t, double[] result, double... controlPoints)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t not in [0..1]");
        }
        if (result.length < 2)
        {
            throw new IllegalArgumentException("result needs size 2");
        }
        if (controlPoints.length != 2*length)
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
        result[0] = x;
        result[1] = y;
    }
    
}
