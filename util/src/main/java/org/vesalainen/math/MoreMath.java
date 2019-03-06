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

import java.util.function.DoubleUnaryOperator;

/**
 * @author Timo Vesalainen
 */
public final class MoreMath 
{
    public static final double EPSILON = 2.220446e-16;
    public static final double SQRT_EPSILON = Math.sqrt(2.220446e-16);
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
    public static double derivate(DoubleUnaryOperator f, double x)
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
}
