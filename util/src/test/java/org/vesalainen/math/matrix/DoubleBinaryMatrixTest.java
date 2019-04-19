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
package org.vesalainen.math.matrix;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleBinaryMatrixTest
{
    
    public DoubleBinaryMatrixTest()
    {
    }

    @Test
    public void testMultiply()
    {
        DoubleBinaryMatrix m1 = DoubleBinaryMatrix.getInstance(2, 2, 3, 4, 1, 0, 0);
        DoubleBinaryMatrix m2 = DoubleBinaryMatrix.getInstance(3, 0, 1000, 1, 100, 0, 10);
        DoubleBinaryMatrix res = DoubleBinaryMatrix.multiply(m1, m2);
        assertEquals(3, res.get(0, 0).applyAsDouble(0,0), 1e-10);
        assertEquals(2340, res.get(0, 1).applyAsDouble(0,0), 1e-10);
        assertEquals(0, res.get(1, 0).applyAsDouble(0,0), 1e-10);
        assertEquals(1000, res.get(1, 1).applyAsDouble(0,0), 1e-10);
    }
    @Test
    public void testDeterminant()
    {
        DoubleBinaryMatrix m = DoubleBinaryMatrix.getInstance(3, -2, 2, -3, -1, 1, 3, 2, 0, -1);
        DoubleBinaryOperator determinant = m.determinant();
        assertEquals(18, determinant.applyAsDouble(0,0), 1e-10);
    }
    
}
