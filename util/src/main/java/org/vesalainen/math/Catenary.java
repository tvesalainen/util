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

import static java.lang.Math.*;
import static org.vesalainen.math.MoreMath.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Catenary implements MathFunction
{
    private double a;
    private double b;
    /**
     * Creates catenary a*cosh(x/a)
     * @param a 
     */
    public Catenary(double a)
    {
        this(a, 0);
    }
    /**
     * Creates catenary with y-shift a*cosh(x/a)+b
     * @param a
     * @param b 
     */
    public Catenary(double a, double b)
    {
        if (a <= 0)
        {
            throw new IllegalArgumentException("a must be positive");
        }
        this.a = a;
        this.b = b;
    }
    
    @Override
    public double applyAsDouble(double x)
    {
        return a*cosh(x/a)+b;
    }

    @Override
    public MathFunction inverse()
    {
        return (x)->a*arcosh((x-b)/a);
    }

    @Override
    public MathFunction arcLength()
    {
        return (x)->a*sinh(x/a);
    }
    /**
     * Returns x for arc length s.
     * @param s
     * @return 
     */
    public MathFunction xForArc()
    {
        return (s)->a*arsinh(s/a);
    }

    @Override
    public MathFunction derivative()
    {
        return (x)->sinh(x/a); // s/a
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
        return solve(
                (a)->a*cosh(x/a)-a, 
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
        return solve(
                (a)->a*arcosh(targetX/a), 
                targetY, 
                Double.MIN_VALUE, 
                10);
    }
}
