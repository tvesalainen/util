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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolynomialTest
{
    
    public PolynomialTest()
    {
    }

    @Test
    public void test0()
    {
        Polynomial p = new Polynomial(0, 0, 1);
        assertEquals(2, p.degree());
        assertEquals(0, p.applyAsDouble(0), 1e-10);
        assertEquals(1, p.applyAsDouble(1), 1e-10);
        assertEquals(4, p.applyAsDouble(2), 1e-10);
        assertEquals(4, p.applyAsDouble(-2), 1e-10);
    }
    @Test
    public void testDerivate()
    {
        Polynomial p = new Polynomial(0, 0, 1);
        MathFunction d = p.derivative();
        assertEquals(0, d.applyAsDouble(0), 1e-10);
        assertEquals(2, d.applyAsDouble(1), 1e-10);
        assertEquals(-2, d.applyAsDouble(-1), 1e-10);
    }    
    @Test
    public void test1()
    {
        Polynomial p = new Polynomial(2, 3, 4, 5, 6, 7);
        assertEquals(-1321, p.applyAsDouble(-3), 1e-10);
    }
    @Test
    public void testIntegral1()
    {
        Polynomial p = new Polynomial(0, 0, 1);
        assertEquals(1.0/3.0, p.integral(0, 1), 1e-10);
    }
    @Test
    public void testIntegral2()
    {
        Polynomial p = new Polynomial(0, 1);
        assertEquals(0.5, p.integral(0, 1, 2), 1e-10);
    }
    @Test
    public void testArc()
    {
        Polynomial p = new Polynomial(0, 1, 0);
        assertEquals(1, p.degree());
        assertEquals(Math.sqrt(2), p.arcLength(0, 1, 2), 1e-10);
    }
}
