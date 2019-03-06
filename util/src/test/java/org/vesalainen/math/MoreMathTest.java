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
    public void testDerivate()
    {
        Polynomial p = new Polynomial(1, 2, 3, 4);
        MathFunction derivate = p.derivative();
        assertEquals(derivate.applyAsDouble(PI), MoreMath.derivate(p, PI), 1e-6);
    }
    @Test
    public void testFactorial()
    {
        assertEquals(24, MoreMath.factorial(4));
    }
    
}
