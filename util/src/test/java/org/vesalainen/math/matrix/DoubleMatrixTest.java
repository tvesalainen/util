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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleMatrixTest
{
    
    public DoubleMatrixTest()
    {
    }

    @Test
    public void test0()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
                assertEquals(i*j, m1.get(i, j), 1e-10);
            }
        }
    }
    @Test
    public void testClone()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
            }
        }
        DoubleMatrix m2 = m1.clone();
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                assertEquals(i*j, m2.get(i, j), 1e-10);
            }
        }
    }
    @Test
    public void testAdd()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
            }
        }
        DoubleMatrix m2 = m1.clone();
        DoubleMatrix m3 = m1.add(m2);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                assertEquals(m1.get(i, j)+m2.get(i, j), m3.get(i, j), 1e-10);
            }
        }
    }    
    @Test
    public void testScalarMultiply()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
            }
        }
        DoubleMatrix m2 = m1.multiply(2);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                assertEquals(2*m1.get(i, j), m2.get(i, j), 1e-10);
            }
        }
    }    
    @Test
    public void testTranspose()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
            }
        }
        DoubleMatrix m2 = m1.transpose();
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                assertEquals(m1.get(i, j), m2.get(j, i), 1e-10);
            }
        }
    }    
    @Test
    public void testToString()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 2, 3, 4, 1, 0, 0);
        assertEquals("2.0 3.0 4.0 \n1.0 0.0 0.0 \n", m1.toString());
    }
    @Test
    public void testMultiply()
    {
        DoubleMatrix m1 = DoubleMatrix.getInstance(2, 2, 3, 4, 1, 0, 0);
        DoubleMatrix m2 = DoubleMatrix.getInstance(3, 0, 1000, 1, 100, 0, 10);
        DoubleMatrix exp = DoubleMatrix.getInstance(2, 3, 2340, 0, 1000);
        assertEquals(exp, DoubleMatrix.multiply(m1, m2));
    }
    @Test
    public void testIdentity()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        DoubleMatrix id = DoubleMatrix.identity(3);
        DoubleMatrix mul = DoubleMatrix.multiply(m, id);
        assertEquals(m, mul);
    }
    @Test
    public void testSwapRows1()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        DoubleMatrix exp = DoubleMatrix.getInstance(3, 1, 2, 3, 7, 8, 9, 4, 5, 6);
        m.swapRows(1, 2);
        assertEquals(exp, m);
    }
    @Test
    public void testSwapRows2()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        DoubleMatrix exp = DoubleMatrix.getInstance(3, 1, 2, 3, 7, 8, 9, 4, 5, 6);
        m.swapRows(1, 2, new double[3]);
        assertEquals(exp, m);
    }
    @Test
    public void testAddEtc()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        DoubleMatrix exp = DoubleMatrix.getInstance(3, 2, 1, 6, 2, 5, 6, 7, 8, 9);
        m.add(0, 0, 1);
        m.sub(0, 1, 1);
        m.mul(0, 2, 2);
        m.div(1, 0, 2);
        assertEquals(exp, m);
    }
    @Test
    public void testDeterminant()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, -2, 2, -3, -1, 1, 3, 2, 0, -1);
        m.decompose();
        assertEquals(18, m.determinant(), 1e-10);
        assertEquals(18, m.permutationDeterminant(), 1e-10);
    }
    @Test
    public void testInvert()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(2, -1, 1.5, 1, -1);
        m.decompose();
        assertEquals(-0.5, m.determinant(), 1e-10);
        DoubleMatrix im = m.invert();
        DoubleMatrix id = DoubleMatrix.identity(2);
        assertEquals(id, DoubleMatrix.multiply(m, im));
    }
    @Test
    public void testSolve()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 3, 2, -1, 2, -2, 4, -1, 0.5, -1);
        DoubleMatrix b = DoubleMatrix.getInstance(3, 1, -2, 0);
        DoubleMatrix exp = DoubleMatrix.getInstance(3, 1, -2, -2);
        DoubleMatrix x = DoubleMatrix.getInstance(3, 1);
        m.decompose();
        m.solve(b, x);
        assertEquals(exp, x);
    }
}
