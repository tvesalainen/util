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
    public void test1()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("x", "t");
        Polynom p1 = b.create("a1", "b1");
        Polynom p2 = b.create("a2", "b2");
        Polynom minus = b.minus(p1, p2);
        Polynom mul = b.mul(minus, minus);
        Polynom der = b.derivative(mul);
        System.err.println(b.subVars("double"));
    }
    
}
