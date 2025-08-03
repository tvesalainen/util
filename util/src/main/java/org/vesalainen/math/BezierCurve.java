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
import org.vesalainen.math.matrix.DoubleUnaryMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 * BezierCurve calculates n-degree Bezier curve
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="bb_bezier.pdf">http://www.math.ucla.edu/~baker/149.1.02w/handouts/bb_bezier.pdf</a>
 */
public class BezierCurve
{
    /**
     * 1-degree
     */
    public static final BezierCurve LINE = new BezierCurve(1);
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
    protected BezierCurve(int degree)
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
    public int getDegree()
    {
        return length-1;
    }
    /**
     * Creates Bezier function for fixed control points.
     * @param controlPoints
     * @return 
     */
    public ParameterizedOperator operator(Point2D... controlPoints)
    {
        if (controlPoints.length != length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        double[] cp = convert(controlPoints);
        return operator(cp);
    }
    /**
     * Creates Bezier function for fixed control points. Note that it is not same
     * if you pass an array or separate parameters. Array is not copied, so if
     * you modify it it will make change. If you want to have function with
     * immutable control points, use separate parameters or copy the array.
     * @param controlPoints
     * @return 
     */
    public ParameterizedOperator operator(double... controlPoints)
    {
        if (controlPoints.length != 2*length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        return operator(controlPoints, 0);
    }
    public ParameterizedOperator operator(double[] controlPoints, int offset)
    {
        return new Operator(getDegree(), controlPoints, offset);
    }
    /**
     * Create first derivative function for fixed control points.
     * @param controlPoints
     * @return 
     */
    private ParameterizedOperator derivative(double... controlPoints)
    {
        if (controlPoints.length != 2*length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        return derivative(controlPoints, 0);
    }
    private ParameterizedOperator derivative(double[] controlPoints, int offset)
    {
        int degree = getDegree();
        double[] cp = derivativeControlPoints(degree, controlPoints, offset);
        if (degree == 1)
        {
            return (t,c)->c.accept(cp[0], cp[1]);
        }
        else
        {
            BezierCurve bc = getInstance(degree-1);
            return bc.operator(cp);
        }
    }
    protected static double[] derivativeControlPoints(int degree, double[] controlPoints, int offset)
    {
        int length = degree+1;
        double[] cp = new double[2*length-2];
        for (int ii=0;ii<length-1;ii++)
        {
            cp[2*ii] = degree*(controlPoints[2*(ii+1)+offset]-controlPoints[2*ii+offset]);
            cp[2*ii+1] = degree*(controlPoints[2*(ii+1)+1+offset]-controlPoints[2*ii+1+offset]);
        }
        return cp;
    }
    /**
     * Create second derivative function for fixed control points.
     * @param controlPoints
     * @return 
     */
    private ParameterizedOperator secondDerivative(double... controlPoints)
    {
        if (controlPoints.length != 2*length)
        {
            throw new IllegalArgumentException("control-points length not "+length);
        }
        return secondDerivative(controlPoints, 0);
    }
    private ParameterizedOperator secondDerivative(double[] controlPoints, int offset)
    {
        int degree = getDegree();
        double[] cp1 = derivativeControlPoints(degree, controlPoints, offset);
        double[] cp2 = derivativeControlPoints(degree-1, cp1, 0);
        if (degree == 2)
        {
            return (t,c)->c.accept(cp2[0], cp2[1]);
        }
        else
        {
            BezierCurve bc = getInstance(degree-2);
            return bc.operator(cp2);
        }
    }
    /**
     * Creates or gets BezierCurve instance
     * @param degree
     * @return 
     */
    public static BezierCurve getInstance(int degree)
    {
        if (degree < 1)
        {
            throw new IllegalArgumentException("illegal degree");
        }
        switch (degree)
        {
            case 1:
                return LINE;
            case 2:
                return QUAD;
            case 3:
                return CUBIC;
            default:
                return new BezierCurve(degree);
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
        calc(t, result, controlPoints, 0);
    }
    public void calc(double t, DoubleBiConsumer result, double[] controlPoints, int offset)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t not in [0..1]");
        }
        double x = 0;
        double y = 0;
        for (int n=0;n<length;n++)
        {
            double c = array[n].applyAsDouble(t);
            x += controlPoints[2*n+offset]*c;
            y += controlPoints[2*n+1+offset]*c;
        }
        result.accept(x, y);
    }
    public double calcX(double t, double[] controlPoints, int offset)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t not in [0..1]");
        }
        double x = 0;
        for (int n=0;n<length;n++)
        {
            double c = array[n].applyAsDouble(t);
            x += controlPoints[2*n+offset]*c;
        }
        return x;
    }
    public double calcY(double t, double[] controlPoints, int offset)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t not in [0..1]");
        }
        double y = 0;
        for (int n=0;n<length;n++)
        {
            double c = array[n].applyAsDouble(t);
            y += controlPoints[2*n+1+offset]*c;
        }
        return y;
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
        Point2D.Double p1 = new Point2D.Double();
        Point2D.Double p2 = new Point2D.Double();
        double sum = 0;
        calc(0, p1::setLocation, controlPoints);
        for (double t=delta;t<1.0;t+=delta)
        {
            calc(t, p2::setLocation, controlPoints);
            sum += p1.distance(p2);
            p1.setLocation(p2);
        }
        calc(1.0, p2::setLocation, controlPoints);
        sum += p1.distance(p2);
        return sum;
    }
    /**
     * Estimates path length so that most of the time estimated path is longer
     * that actual.
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
     * Estimates path length so that most of the time estimated path is longer
     * that actual.
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
    private static double[] convert(Point2D... points)
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

    private class Operator implements ParameterizedOperator
    {
        private int degree;
        private double[] controlPoints;
        private int offset;
        private DoubleUnaryMatrix derivative;
        private DoubleUnaryMatrix secondDerivative;

        public Operator(int degree, double... controlPoints)
        {
            this(degree, controlPoints, 0);
        }
        public Operator(int degree, double[] controlPoints, int offset)
        {
            this.degree = degree;
            this.controlPoints = controlPoints;
            this.offset = offset;
        }
        
        @Override
        public void calc(double t, DoubleBiConsumer consumer)
        {
            BezierCurve.this.calc(t, consumer, controlPoints, offset);
        }

        @Override
        public double calcX(double t)
        {
            return BezierCurve.this.calcX(t, controlPoints, offset);
        }

        @Override
        public double calcY(double t)
        {
            return BezierCurve.this.calcY(t, controlPoints, offset);
        }

        @Override
        public DoubleUnaryMatrix derivative()
        {
            if (derivative == null)
            {
                if (degree == 1)
                {
                    derivative = DoubleUnaryMatrix.getInstance(2, 
                            controlPoints[2]-controlPoints[0],
                            controlPoints[3]-controlPoints[1]
                    );
                }
                else
                {
                    double[] cp = derivativeControlPoints(degree, controlPoints, offset);
                    BezierCurve bc = getInstance(degree-1);
                    derivative = new DoubleUnaryMatrix(2, 
                            (t)->bc.calcX(t, cp, 0),
                            (t)->bc.calcY(t, cp, 0)
                    );
                }
            }
            return derivative;
        }

        @Override
        public DoubleUnaryMatrix secondDerivative()
        {
            if (secondDerivative == null)
            {
                double[] cp1 = derivativeControlPoints(degree, controlPoints, offset);
                double[] cp2 = derivativeControlPoints(degree-1, cp1, 0);
                BezierCurve bc = getInstance(degree-2);
                secondDerivative = new DoubleUnaryMatrix(2, 
                        (t)->bc.calcX(t, cp2, 0),
                        (t)->bc.calcY(t, cp2, 0)
                );
            }
            return secondDerivative;
        }

        @Override
        public double evalTForX(double x, double deltaX)
        {
            double s1 = controlPoints[offset];
            double s2 = controlPoints[offset+2*degree];
            if (x < s1 || x > s2)
            {
                throw new IllegalArgumentException("out of range");
            }
            if (x == s1)
            {
                return 0;
            }
            if (x == s2)
            {
                return 1;
            }
            double t = (x - s1) / (s2 - s1);
            double ex = calcX(t);
            int count = 0;
            while (Math.abs(x - ex) > deltaX)
            {
                if (count > 128)
                {
                    throw new IllegalArgumentException("deltaX too small");
                }
                double d = x - ex;
                t += d / derivative().eval(0, 0, t);
                if (t < 0)
                {
                    t = 0;
                }
                else
                {
                    if (t > 1)
                    {
                        t = 1;
                    }
                }
                ex = calcX(t);
                count++;
            }
            return t;
        }

        @Override
        public double evalTForY(double y, double deltaY)
        {
            double s1 = controlPoints[offset+1];
            double s2 = controlPoints[offset+2*degree+1];
            if (y < s1 || y > s2)
            {
                throw new IllegalArgumentException("out of range");
            }
            if (y == s1)
            {
                return 0;
            }
            if (y == s2)
            {
                return 1;
            }
            double t = (y - s1) / (s2 - s1);
            double ey = calcY(t);
            int count = 0;
            while (Math.abs(y - ey) > deltaY)
            {
                if (count > 128)
                {
                    throw new IllegalArgumentException("deltaX too small");
                }
                double d = y - ey;
                t += d / derivative().eval(1, 0, t);
                ey = calcY(t);
                count++;
            }
            return t;
        }
        
    }
}
