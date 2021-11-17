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

import org.ejml.data.DenseMatrix64F;
import static org.ejml.ops.CommonOps.add;
import static org.ejml.ops.SpecializedOps.diffNormF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleMatrixTest
{
    private static final double Epsilon = 1e-10;
    
    public DoubleMatrixTest()
    {
    }

    @Test
    public void test0()
    {
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
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
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
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
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
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
    public void testSubtract()
    {
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                m1.set(i, j, i*j);
            }
        }
        DoubleMatrix m2 = m1.clone();
        DoubleMatrix m3 = m1.subtract(m2);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<3;j++)
            {
                assertEquals(m1.get(i, j)-m2.get(i, j), m3.get(i, j), 1e-10);
            }
        }
    }    
    @Test
    public void testScalarMultiply()
    {
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
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
        DoubleMatrix m1 = new DoubleMatrix(2, 3);
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
        assertEquals("2.000 3.000 4.000 \n1.000 0.000 0.000 \n", m1.toString());
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
        DoubleMatrix x = new DoubleMatrix(3, 1);
        m.decompose();
        m.solve(b, x);
        assertEquals(exp, x);
    }
    @Test
    public void testSolve2()
    {
        DoubleMatrix m = DoubleMatrix.getInstance(3, 3, 2, -1, 2, -2, 4, -1, 0.5, -1);
        DoubleMatrix b = DoubleMatrix.getInstance(3, 1, -2, 0);
        DoubleMatrix exp = DoubleMatrix.getInstance(3, 1, -2, -2);
        DoubleMatrix x = new DoubleMatrix(3, 1);
        DoubleMatrix.solve(m, b, x);
        assertEquals(exp, x);
    }
    @Test
    public void testMultTransB()
    {
        DoubleMatrix a = new DoubleMatrix(2, new double[]{1, 2, 3, 4, 5, 6});
        DoubleMatrix b = new DoubleMatrix(2, new double[]{10, 20, 30, 40, 50, 60});
        DoubleMatrix c = new DoubleMatrix(2, 2);
        DoubleMatrix transpose = b.transpose();
        DoubleMatrix multiply = a.multiply(transpose);
        DoubleMatrix.multTransB(a, b, c);
        assertEquals(multiply, c);
    }
    @Test
    public void testMultTransB2()
    {
        DoubleMatrix a = new DoubleMatrix(3, new double[]{1, 2, 3, 4, 5, 6});
        DoubleMatrix b = new DoubleMatrix(3, new double[]{10, 20, 30, 40, 50, 60});
        DoubleMatrix c = new DoubleMatrix(3, 3);
        DoubleMatrix transpose = b.transpose();
        DoubleMatrix multiply = a.multiply(transpose);
        DoubleMatrix.multTransB(a, b, c);
        assertEquals(multiply, c);
    }
    @Test
    public void testDiffNorm()
    {
        DenseMatrix64F da = new DenseMatrix64F(2, 2, true, new double[]{1, 2, 3, 4});
        DenseMatrix64F db = new DenseMatrix64F(2, 2, true, new double[]{10, 20, 30, 40});
        DoubleMatrix a = new DoubleMatrix(2, new double[]{1, 2, 3, 4});
        DoubleMatrix b = new DoubleMatrix(2, new double[]{10, 20, 30, 40});
        double exp = diffNormF(da, db);
        double got = DoubleMatrix.diffNorm(a, b);
        assertEquals(exp, got, 1e-10);
    }
    @Test
    public void testAdd9()
    {
        DenseMatrix64F da = new DenseMatrix64F(2, 2, true, new double[]{1, 2, 3, 4});
        DenseMatrix64F db = new DenseMatrix64F(2, 2, true, new double[]{10, 20, 30, 40});
        DenseMatrix64F dc = new DenseMatrix64F(2, 2, true, new double[]{1, 2, 3, 4});
        DoubleMatrix a = new DoubleMatrix(2, new double[]{1, 2, 3, 4});
        DoubleMatrix b = new DoubleMatrix(2, new double[]{10, 20, 30, 40});
        DoubleMatrix c = new DoubleMatrix(2, new double[]{1, 2, 3, 4});
        double alpha = 1.23;
        double beta = 4.56;
        add(alpha, da, beta, db, dc);
        DoubleMatrix.add(alpha, a, beta, b, c);
        assertArrayEquals(dc.data, (double[]) c.array, 1e-10);
    }
    @Test
    public void testRemoveRow1()
    {
        DoubleMatrix x = new DoubleMatrix(1, 3);
        x.addRow(1, 2, 3);
        x.addRow(4, 5, 6);
        x.addRow(7, 8, 9);
        x.removeRowAt(2);
        assertEquals(3, x.rows());
        assertTrue(x.containsRow(1, 2, 3));
        assertFalse(x.containsRow(4, 5, 6));
        assertTrue(x.containsRow(7, 8, 9));
    }
    @Test
    public void testRemoveRow2()
    {
        DoubleMatrix x = new DoubleMatrix(1, 3);
        x.addRow(1, 2, 3);
        x.addRow(4, 5, 6);
        x.addRow(7, 8, 9);
        x.removeRow(4, 5, 6);
        assertEquals(3, x.rows());
        assertTrue(x.containsRow(1, 2, 3));
        assertFalse(x.containsRow(4, 5, 6));
        assertTrue(x.containsRow(7, 8, 9));
    }
    @Test
    public void testContainsRow1()
    {
        DoubleMatrix x = new DoubleMatrix(1, 3);
        x.addRow(1, 2, 3);
        x.addRow(4, 5, 6);
        x.addRow(7, 8, 9);
        assertTrue(x.containsRow(1, 2, 3));
        assertTrue(x.containsRow(4, 5, 6));
        assertTrue(x.containsRow(7, 8, 9));
    }
    @Test
    public void testInsertRow1()
    {
        DoubleMatrix x = new DoubleMatrix(1, 3);
        x.addRow(1, 2, 3);
        x.addRow(4, 5, 6);
        x.addRow(7, 8, 9);
        assertEquals(4, x.rows());
        x.insertRow(1, -1, -2, -3);
        assertEquals(5, x.rows());
        assertEquals(-1, x.get(1, 0), Epsilon);
        assertEquals(-2, x.get(1, 1), Epsilon);
        assertEquals(-3, x.get(1, 2), Epsilon);
        x.setRow(2, 10, 20, 30);
        assertEquals(5, x.rows());
        assertEquals(10, x.get(2, 0), Epsilon);
        assertEquals(20, x.get(2, 1), Epsilon);
        assertEquals(30, x.get(2, 2), Epsilon);
    }
    @Test
    public void testRemoveEqualRows1()
    {
        DoubleMatrix x = new DoubleMatrix(10, 1, true,
                1,
                1,
                3,
                1,
                1,
                1,
                1,
                6,
                6,
                6
        );
        x.removeEqualRows();
        assertEquals(4, x.rows());
        int index = 0;
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(3, x.data(index++), Epsilon);
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(6, x.data(index++), Epsilon);
    }
    @Test
    public void testRemoveEqualRows2()
    {
        DoubleMatrix x = new DoubleMatrix(10, 1, true,
                1,
                3,
                3,
                1,
                4,
                1,
                1,
                5,
                5,
                6
        );
        x.removeEqualRows();
        assertEquals(7, x.rows());
        int index = 0;
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(3, x.data(index++), Epsilon);
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(4, x.data(index++), Epsilon);
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(5, x.data(index++), Epsilon);
        assertEquals(6, x.data(index++), Epsilon);
    }
    @Test
    public void testRemoveEqualRows3()
    {
        DoubleMatrix x = new DoubleMatrix(10, 1, true,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                0
        );
        x.removeEqualRows();
        assertEquals(10, x.rows());
        int index = 0;
        assertEquals(1, x.data(index++), Epsilon);
        assertEquals(2, x.data(index++), Epsilon);
        assertEquals(3, x.data(index++), Epsilon);
        assertEquals(4, x.data(index++), Epsilon);
        assertEquals(5, x.data(index++), Epsilon);
        assertEquals(6, x.data(index++), Epsilon);
        assertEquals(7, x.data(index++), Epsilon);
        assertEquals(8, x.data(index++), Epsilon);
        assertEquals(9, x.data(index++), Epsilon);
        assertEquals(0, x.data(index++), Epsilon);
    }
    @Test
    public void testRemoveEqualRows4()
    {
        DoubleMatrix x = new DoubleMatrix(5, 2, true,
                1, 2, 
                3, 4,
                5, 6,
                7, 8,
                9, 0
        );
        DoubleMatrix exp = x.clone();
        x.removeEqualRows();
        assertEquals(5, x.rows());
        assertEquals(exp, x);
    }
    @Test
    public void testRemoveEqualRows5()
    {
        DoubleMatrix x = new DoubleMatrix(5, 2, true,
                1, 2,
                3, 4,
                3, 4,
                7, 8,
                9, 0
        );
        x.removeEqualRows();
        assertEquals(4, x.rows());
        assertEquals(0, x.findRow(1, 2));
        assertEquals(1, x.findRow(3, 4));
        assertEquals(2, x.findRow(7, 8));
        assertEquals(3, x.findRow(9, 0));
    }
    @Test
    public void testRemoveEqualRows6()
    {
        DoubleMatrix x = new DoubleMatrix(6, 2, true,
                1, 2,
                3, 4,
                3, 4,
                7, 8,
                9, 0,
                1, 2
        );
        x.removeEqualRows();
        assertEquals(4, x.rows());
        assertEquals(0, x.findRow(1, 2));
        assertEquals(1, x.findRow(3, 4));
        assertEquals(2, x.findRow(7, 8));
        assertEquals(3, x.findRow(9, 0));
    }
}
