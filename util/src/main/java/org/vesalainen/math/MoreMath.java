/*
 * Copyright (C) 2012 Timo Vesalainen
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
import static java.lang.Math.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.math.matrix.DoubleUnaryMatrix;
import org.vesalainen.util.function.DoubleBiPredicate;

/**
 * @author Timo Vesalainen
 */
public final class MoreMath 
{
    public static final double EPSILON = 2.220446e-16;
    public static final double SQRT_EPSILON = Math.sqrt(2.220446e-16);
    private static final ThreadLocal<Point2D.Double> PNT1 = ThreadLocal.withInitial(Point2D.Double::new);
    /**
     * Returns sum of arguments throwing an exception if the result overflows an int.
     * @param x
     * @return 
     */
    public static int sum(int... x)
    {
        int s = 0;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            s = Math.addExact(s, x[ii]);
        }
        return s;
    }
    /**
     * Returns long sum of arguments throwing an exception if the result overflows an int.
     * @param x
     * @return 
     */
    public static long longSum(int... x)
    {
        long s = 0;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            s = Math.addExact(s, (long)x[ii]);
        }
        return s;
    }
    /**
     * Returns x^exp throwing an exception if the result overflows an int.
     * @param x
     * @param exp
     * @return 
     */
    public static int power(int x, int exp)
    {
        if (exp == 0)
        {
            return 1;
        }
        int e = Math.abs(exp);
        int s = 1;
        for (int ii=0;ii<e;ii++)
        {
            s = Math.multiplyExact(s, x);
        }
        if (exp > 0)
        {
            return s;
        }
        else
        {
            return 1/s;
        }
    }
    /**
     * Returns sum of arguments throwing an exception if the result overflows an long.
     * @param x
     * @return 
     */
    public static long sum(long... x)
    {
        long s = 0;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            s = Math.addExact(s, x[ii]);
        }
        return s;
    }
    /**
     * Returns x^exp throwing an exception if the result overflows an long.
     * @param x
     * @param exp
     * @return 
     */
    public static long power(long x, int exp)
    {
        if (exp == 0)
        {
            return 1;
        }
        int e = Math.abs(exp);
        long s = 1;
        for (int ii=0;ii<e;ii++)
        {
            s = Math.multiplyExact(s, x);
        }
        if (exp > 0)
        {
            return s;
        }
        else
        {
            return 1/s;
        }
    }
    /**
     * Returns logarithm with base b.
     * @param b
     * @param x
     * @return 
     */
    public static double log(double b, double x)
    {
        return Math.log(x)/Math.log(b);
    }
    /**
     * Returns sqrt(MACHINE_EPSILON)*x or if x == 0 sqrt(MACHINE_EPSILON)
     * @param x
     * @return 
     */
    public static double sqrtEpsilon(double x)
    {
        return x != 0.0 ? SQRT_EPSILON*x : SQRT_EPSILON;
    }
    /**
     * Returns numerical integral between x1 and x2
     * @param x1
     * @param x2
     * @param points
     * @return 
     */
    public static double integral(DoubleUnaryOperator f, double x1, double x2, int points)
    {
        double delta = (x2-x1)/points;
        double delta2 = delta/2.0;
        double sum = 0;
        double y1 = f.applyAsDouble(x1);
        double y2;
        for (int ii=1;ii<=points;ii++)
        {
            x1 += delta;
            y2 = f.applyAsDouble(x1);
            sum += (y1+y2)*delta2;
            y1 = y2;
        }
        return sum;
    }
    public static double derivative(DoubleUnaryOperator f, double x)
    {
        double h = x != 0.0 ? SQRT_EPSILON*x : SQRT_EPSILON;
        double h2 = 2.0*h;
        double y1 = -f.applyAsDouble(x+h2);
        double y2 = 8.0*f.applyAsDouble(x+h);
        double y3 = -8.0*f.applyAsDouble(x-h);
        double y4 = f.applyAsDouble(x-h2);
        return (y1+y2+y3+y4)/(12.0*h);
    }
    /**
     * Return partial derivative x
     * @param f
     * @return 
     */
    public static DoubleBinaryOperator dx(DoubleBinaryOperator f)
    {
        return (x,y)->
        {
            double h = x != 0.0 ? SQRT_EPSILON*x : SQRT_EPSILON;
            double h2 = 2.0*h;
            double y1 = -f.applyAsDouble(x+h2, y);
            double y2 = 8.0*f.applyAsDouble(x+h, y);
            double y3 = -8.0*f.applyAsDouble(x-h, y);
            double y4 = f.applyAsDouble(x-h2, y);
            return (y1+y2+y3+y4)/(12.0*h);
        };
    }
    /**
     * Return partial derivative x
     * @param f
     * @return 
     */
    public static DoubleBinaryOperator dy(DoubleBinaryOperator f)
    {
        return (x,y)->
        {
            double h = y != 0.0 ? SQRT_EPSILON*y : SQRT_EPSILON;
            double h2 = 2.0*h;
            double y1 = -f.applyAsDouble(x, y+h2);
            double y2 = 8.0*f.applyAsDouble(x, y+h);
            double y3 = -8.0*f.applyAsDouble(x, y-h);
            double y4 = f.applyAsDouble(x, y-h2);
            return (y1+y2+y3+y4)/(12.0*h);
        };
    }
    /**
     * Returns Jacobian matrix
     * @return 
     */
    public static DoubleBinaryMatrix gradient(DoubleTransform t)
    {
        return new DoubleBinaryMatrix(2,
                MoreMath.dx(t.fx()),
                MoreMath.dy(t.fx()),
                MoreMath.dx(t.fy()),
                MoreMath.dy(t.fy())
        );
    }
    public static DoubleUnaryOperator dx(ParameterizedOperator op)
    {
        return (t)->
        {
            double h = t != 0.0 ? SQRT_EPSILON*t : SQRT_EPSILON;
            double h2 = 2.0*h;
            double y1 = -op.calcX(t+h2);
            double y2 = 8.0*op.calcX(t+h);
            double y3 = -8.0*op.calcX(t-h);
            double y4 = op.calcX(t-h2);
            return (y1+y2+y3+y4)/(12.0*h);
        };
    }
    public static DoubleUnaryOperator dy(ParameterizedOperator op)
    {
        return (t)->
        {
            double h = t != 0.0 ? SQRT_EPSILON*t : SQRT_EPSILON;
            double h2 = 2.0*h;
            double y1 = -op.calcY(t+h2);
            double y2 = 8.0*op.calcY(t+h);
            double y3 = -8.0*op.calcY(t-h);
            double y4 = op.calcY(t-h2);
            return (y1+y2+y3+y4)/(12.0*h);
        };
    }
    public static DoubleUnaryMatrix gradient(ParameterizedOperator op)
    {
        return new DoubleUnaryMatrix(2,
                MoreMath.dx(op),
                MoreMath.dy(op)
        );
    }
    public static ParameterizedOperator derivative(ParameterizedOperator op)
    {
        return (t,c)->
        {
            double h = t != 0.0 ? SQRT_EPSILON*t : SQRT_EPSILON;
            double h2 = 2.0*h;
            double h12 = 12.0*h;
            Point2D.Double p = PNT1.get();
            op.calc(t+h2, (xx,yy)->p.setLocation(-xx, -yy));
            op.calc(t+h, (xx,yy)->p.setLocation(p.x+8*xx, p.y+8*yy));
            op.calc(t-h, (xx,yy)->p.setLocation(p.x-8*xx, p.y-8*yy));
            op.calc(t-h2, (xx,yy)->p.setLocation(p.x+xx, p.y+yy));
            c.accept(p.x/h12, p.y/h12);
        };
    }
    /**
     * Returns numerical arc length between x1 and x2
     * @param x1
     * @param x2
     * @param points
     * @return 
     */
    public static double arcLength(DoubleUnaryOperator f, double x1, double x2, int points)
    {
        double delta = (x2-x1)/points;
        double sum = 0;
        double y1 = f.applyAsDouble(x1);
        double y2;
        for (int ii=1;ii<=points;ii++)
        {
            x1 += delta;
            y2 = f.applyAsDouble(x1);
            sum += Math.hypot(delta, (y2-y1));
            y1 = y2;
        }
        return sum;
    }
    /**
     * Returns k!
     * @param k
     * @return 
     * @throws ArithmeticException if result exceeds maximum long value.
     */
    public static long factorial(long k)
    {
        long result = 1;
        for (long ii=1;ii<=k;ii++)
        {
            result = Math.multiplyExact(result, ii);
        }
        return result;
    }
    /**
     * Returns k!
     * @param k
     * @return 
     * @throws ArithmeticException if result exceeds maximum int value.
     */
    public static int factorial(int k)
    {
        int result = 1;
        for (int ii=1;ii<=k;ii++)
        {
            result = Math.multiplyExact(result, ii);
        }
        return result;
    }
    /**
     * Returns sinh inverse
     * @param x
     * @return 
     */
    public static double arsinh(double x)
    {
        return Math.log(x+Math.sqrt(x*x+1));
    }
    /**
     * Returns cosh inverse
     * @param x
     * @return 
     */
    public static double arcosh(double x)
    {
        return Math.log(x+Math.sqrt(x*x-1));
    }
    /**
     * Returns coefficient that fulfills targetY = f(targetX, coef)
     * @param f F(x, coef)
     * @param targetX
     * @param targetY
     * @param minCoef
     * @param maxCoef
     * @return 
     */
    public static double solve(
            DoubleBinaryOperator f,
            double targetX,
            double targetY,
            double minCoef,
            double maxCoef
    )
    {
        double coef = (maxCoef-minCoef)/2.0;
        double y = 0;
        double d = coef/2.0;
        int s = 0;
        DoubleBiPredicate test;
        y = f.applyAsDouble(targetX, coef);
        double y2 = f.applyAsDouble(targetX, coef*0.5);
        if (y2 > y)
        {
            test = (l,r)->l>r;
        }
        else
        {
            test = (l,r)->l<r;
        }
        for (int ii=0;ii<128;ii++)
        {
            y = f.applyAsDouble(targetX, coef);
            if (!Double.isFinite(y))
            {
                throw new IllegalArgumentException("Y="+y);
            }
            if (y == y2)
            {
                return coef;
            }
            y2 = y;
            if (test.test(y, targetY))
            {
                if (coef == maxCoef)
                {
                    throw new IllegalArgumentException(coef+" coef out of bounds");
                }
                coef = min(maxCoef, coef+d);
                if (s != 1)
                {
                    d /= 2;
                    s = 1;
                }
            }
            else
            {
                if (coef == minCoef)
                {
                    throw new IllegalArgumentException(coef+" coef out of bounds");
                }
                coef = max(minCoef, coef-d);
                if (s != 2)
                {
                    d /= 2;
                    s = 2;
                }
            }
        }
        throw new IllegalArgumentException();
    }
}
