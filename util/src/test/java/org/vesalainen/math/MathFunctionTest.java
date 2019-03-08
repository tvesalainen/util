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

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MathFunctionTest
{

    private final Random random;
    private final int count;
    private final int max;
    
    public MathFunctionTest()
    {
        random = new Random(1234567L);
        count = 1000;
        max = 100000;
    }

    @Test
    public void testDerivate()
    {
        MathFunction p = (x)->x*x;
        MathFunction d = p.derivative();
        assertEquals(0, d.applyAsDouble(0), 1e-10);
        assertEquals(2, d.applyAsDouble(1), 1e-7);
        assertEquals(-2, d.applyAsDouble(-1), 1e-8);
    }    
    @Test
    public void testIntegral()
    {
        MathFunction p = (x)->x*x;
        assertEquals(1.0/3.0, p.integral(0, 1), 1e-10);
    }
    @Test
    public void testArc()
    {
        MathFunction p = (x)->x;
        assertEquals(Math.sqrt(2), p.arcLength(0, 1), 1e-10);
    }
    
    @Test
    public void testExponential()
    {
        test(new Exponential(3), -300, 300);
    }
    @Test
    public void testPreMultiplier()
    {
        test(MathFunction.preMultiplier(new Logarithm(10), 10), Double.MIN_VALUE, max);
    }
    @Test
    public void testPostMultiplier()
    {
        test(MathFunction.postMultiplier(new Logarithm(10), 10), Double.MIN_VALUE, max);
    }
    @Test
    public void testLn()
    {
        test(new Logarithm(Math.E), Double.MIN_VALUE, max);
    }
    @Test
    public void testLog10()
    {
        test(new Logarithm(10), Double.MIN_VALUE, max);
    }
    @Test
    public void testLog3()
    {
        test(new Logarithm(3), Double.MIN_VALUE, max);
    }
    public void test(MathFunction f, double min, double max)
    {
        testDerivate(f, random, count, min, max);
        testIntegral(f, random, count, min, max);
        testInverse(f, random, count, min, max);
    }

    private void testDerivate(MathFunction f, Random r, int count, double min, double max)
    {
        MathFunction d2 = f.derivative();
        r.doubles(count, mid(min, max), max).forEach((x)->
        {
            assertEq("x="+x, MoreMath.derivate(f, x), d2.applyAsDouble(x), 1e10);
        });
    }

    private void testIntegral(MathFunction f, Random r, int count, double min, double max)
    {
        r.doubles(count, mid(min, max), max).forEach((x)->
        {
            assertEq(MoreMath.integral(f, x, x+1, 60000), f.integral(x, x+1), 1e6);
        });
    }

    private void testInverse(MathFunction f, Random r, int count, double min, double max)
    {
        MathFunction inverse = f.inverse();
        r.doubles(count, mid(min, max), max).forEach((x)->
        {
            double y = f.applyAsDouble(x);
            assertEq(x, inverse.applyAsDouble(y), 1e2);
        });
    }
    private double mid(double min, double max)
    {
        return (max-min)/2.0+min;
    }
    private void assertEq(double exp, double got, double ulps)
    {
        assertEq(null, exp, got, ulps);
    }
    private void assertEq(String msg, double exp, double got, double ulps)
    {
        double ulp = Math.ulp(exp);
        //double d = exp-got;
        assertEquals(msg, exp, got, ulp*ulps);
    }
}
