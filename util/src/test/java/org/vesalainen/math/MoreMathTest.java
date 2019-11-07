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

import static java.lang.Math.PI;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MoreMathTest
{
    
    public MoreMathTest()
    {
    }

    @Test
    public void testSum()
    {
        assertEquals(6, MoreMath.sum(1, 2, 3));
    }
    @Test
    public void testPow()
    {
        assertEquals(16, MoreMath.pow(2, 4));
    }
    @Test
    public void testLog()
    {
        assertEquals(Math.log10(PI), MoreMath.log(10, PI), 1e-10);
    }
    @Test
    public void testDerivate()
    {
        Polynomial p = new Polynomial(1, 2, 3, 4);
        MathFunction derivate = p.derivative();
        assertEquals(derivate.applyAsDouble(PI), MoreMath.derivative(p, PI), 1e-6);
    }
    @Test
    public void testFactorial()
    {
        assertEquals(24, MoreMath.factorial(4));
    }
    @Test
    public void testSolve()
    {
        double res = MoreMath.solve(
                (x,a)->a*a+x,
                1,
                2,
                -10, 
                100);
        assertEquals(1, res, 1e-10);
    }
    @Test
    public void testSinDerivative()
    {
        DoubleUnaryOperator f = (x)->Math.sin(x);
        for (double a=0;a<2*Math.PI;a+=0.2)
        {
            assertEquals(Math.cos(a), MoreMath.derivative(f, a), 1e-8);
        }
    }    
    @Test
    public void testCosDerivative()
    {
        DoubleUnaryOperator f = (x)->Math.cos(x);
        for (double a=0;a<2*Math.PI;a+=0.2)
        {
            assertEquals(-Math.sin(a), MoreMath.derivative(f, a), 1e-7);
        }
    }    
    @Test
    public void testPartialDerivative()
    {
        DoubleBinaryOperator f = (x,y)->x*x*y;
        DoubleBinaryOperator dx = MoreMath.dx(f);
        DoubleBinaryOperator dy = MoreMath.dy(f);
        assertEquals(24, dx.applyAsDouble(3, 4), 1e-6);
        assertEquals(9, dy.applyAsDouble(3, 4), 1e-6);
    }
}
