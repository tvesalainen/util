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
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BernsteinPolynomialTest
{
    private double Epsilon = 1e-10;
    public BernsteinPolynomialTest()
    {
    }

    @Test
    public void test1()
    {
        DoubleUnaryOperator b30 = BernsteinPolynomial.b(3, 0);
        DoubleUnaryOperator b31 = BernsteinPolynomial.b(3, 1);
        DoubleUnaryOperator b32 = BernsteinPolynomial.b(3, 2);
        DoubleUnaryOperator b33 = BernsteinPolynomial.b(3, 3);
        assertEquals(1.0, b30.applyAsDouble(0), Epsilon);
        assertEquals(0.0, b30.applyAsDouble(1), Epsilon);
        assertEquals(0.0, b31.applyAsDouble(1), Epsilon);
        assertEquals(0.0, b31.applyAsDouble(0), Epsilon);
        assertEquals(0.0, b32.applyAsDouble(1), Epsilon);
        assertEquals(0.0, b32.applyAsDouble(0), Epsilon);
        assertEquals(0.0, b33.applyAsDouble(0), Epsilon);
        assertEquals(1.0, b33.applyAsDouble(1), Epsilon);
    }
    @Test
    public void test2()
    {
        DoubleUnaryOperator b30 = BernsteinPolynomial.b(3, 0);
        DoubleUnaryOperator b31 = BernsteinPolynomial.b(3, 1);
        DoubleUnaryOperator b32 = BernsteinPolynomial.b(3, 2);
        DoubleUnaryOperator b33 = BernsteinPolynomial.b(3, 3);
        Random rand = new Random(12345L);
        for (int ii=0;ii<100;ii++)
        {
            double t = rand.nextDouble();
            assertEquals(1.0, b30.applyAsDouble(t)+b31.applyAsDouble(t)+b32.applyAsDouble(t)+b33.applyAsDouble(t), Epsilon);
            assertTrue(b30.applyAsDouble(t) >= 0.0);
            assertTrue(b31.applyAsDouble(t) >= 0.0);
            assertTrue(b32.applyAsDouble(t) >= 0.0);
            assertTrue(b33.applyAsDouble(t) >= 0.0);
        }
    }
    @Test
    public void testC()
    {
        assertEquals(1, BernsteinPolynomial.c(3, 0));
        assertEquals(3, BernsteinPolynomial.c(3, 1));
        assertEquals(3, BernsteinPolynomial.c(3, 2));
        assertEquals(1, BernsteinPolynomial.c(3, 3));
    }
    
}
