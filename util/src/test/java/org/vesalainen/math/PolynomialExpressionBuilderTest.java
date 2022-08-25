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

import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.PolynomialExpressionBuilder.Polynom;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolynomialExpressionBuilderTest
{
    
    public PolynomialExpressionBuilderTest()
    {
    }

    @Test
    public void testPlus()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p = b.create("a*x+b");
        Polynom plus = b.plus(p, p);
        assertEquals("2ax+2b", plus.toString());
    }
    @Test
    public void testMinus()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p = b.create("a*x+b");
        Polynom minus = b.minus(p, p);
        assertEquals("0", minus.toString());
    }
    @Test
    public void testMul1()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p = b.create("a*x+b");
        Polynom mul = b.mul(p, p);
        assertEquals("a²x²+2abx+b²", mul.toString());
    }
    @Test
    public void testMul2()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p = b.create("a*x-b");
        assertEquals("ax-b", p.toString());
        assertEquals("a*x-b", p.toCode());
        Polynom mul = b.mul(p, p);
        assertEquals("a²x²-2abx+b²", mul.toString());
    }
    @Test
    public void testMul3()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p1 = b.create("a*x-b");
        Polynom p2 = b.create("a*x+b");
        Polynom mul = b.mul(p1, p2);
        assertEquals("a²x²-b²", mul.toString());
    }
    @Test
    public void testMul4()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p1 = b.create("x-1");
        Polynom p2 = b.create("x+1");
        Polynom mul = b.mul(p1, p2);
        assertEquals("x²-1", mul.toString());
    }
    @Test
    public void testMul5()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p1 = b.create("x*x-1");
        Polynom p2 = b.create("x*x+1");
        Polynom mul = b.mul(p1, p2);
        assertEquals("x⁴-1", mul.toString());
    }
    @Test
    public void test1()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x");
        Polynom p1 = b.create("b*x+a");
        assertEquals("bx+a", p1.toString());
        assertEquals("b*x+a", p1.toCode());
        Polynom p2 = b.create("c+d*x");
        assertEquals("dx+c", p2.toString());
        assertEquals("d*x+c", p2.toCode());
        Polynom minus = b.minus(p1, p2);
        assertEquals("(b-d)x+(a-c)", minus.toString());
        assertEquals("(b-d)*x+(a-c)", minus.toCode());
        Polynom sq = b.mul(minus, minus);
        assertEquals("(b²-2bd+d²)x²+(2ab-2ad-2bc+2cd)x+(a²-2ac+c²)", sq.toString());
        assertEquals("(b*b-2*b*d+d*d)*x*x+(2*a*b-2*a*d-2*b*c+2*c*d)*x+(a*a-2*a*c+c*c)", sq.toCode());
        Polynom der = sq.derivative();
        assertEquals("(2b²-4bd+2d²)x+(2ab-2ad-2bc+2cd)", der.toString());
        assertEquals("(2*b*b-4*b*d+2*d*d)*x+(2*a*b-2*a*d-2*b*c+2*c*d)", der.toCode());
    }
    
}
