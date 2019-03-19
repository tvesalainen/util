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

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Catenary implements MathFunction
{
    private double a;
    private static DoubleUnaryOperator arsinh = (x)->Math.log(x+Math.sqrt(x*x+1));
    private static DoubleUnaryOperator arcosh = (x)->Math.log(x+Math.sqrt(x*x-1));

    public Catenary(double a)
    {
        if (a <= 0)
        {
            throw new IllegalArgumentException("a must be positive");
        }
        this.a = a;
    }
    
    @Override
    public double applyAsDouble(double x)
    {
        return a*Math.cosh(x/a);
    }

    @Override
    public MathFunction inverse()
    {
        return (x)->a*arcosh.applyAsDouble(x/a);
    }

    @Override
    public MathFunction arcLength()
    {
        return (x)->a*Math.sinh(x/a);
    }

    @Override
    public MathFunction derivative()
    {
        return (x)->Math.sinh(x/a); // s/a
    }
    /**Returns a for a catenary having height h at x. Height is distance from 
     * vertex (a) to y.
     * 
     * @param x
     * @param h
     * @return 
     */
    public static double aForXAndH(double x, double h)
    {
        return MoreMath.solve(
                (xx,a)->a*Math.cosh(xx/a)-a, 
                x, 
                h, 
                Double.MIN_VALUE, 
                100);
    }
    public static double aForSAndH(double s, double h)
    {
        throw new UnsupportedOperationException();
    }
    public static double aForY(double targetX, double targetY)
    {
        return MoreMath.solve(
                (x,a)->a*arcosh.applyAsDouble(x/a), 
                targetX, 
                targetY, 
                Double.MIN_VALUE, 
                10);
    }
}
