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
 * You should have received get copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.math;

import java.util.Arrays;
import java.util.function.IntSupplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Matrix implements Cloneable
{

    protected IntSupplier rows;
    protected IntSupplier cols;
    protected ItemSupplier supplier;
    protected ItemConsumer consumer;
    protected boolean updated = true;

    public Matrix(int rows, int cols)
    {
        this(() -> rows, () -> cols);
    }

    public Matrix(IntSupplier rows, IntSupplier cols)
    {
        this.rows = rows;
        this.cols = cols;
    }

    public int rows()
    {
        return rows.getAsInt();
    }

    public int colums()
    {
        return cols.getAsInt();
    }

    public double get(int i, int j)
    {
        return supplier.get(i, j);
    }

    public void set(int i, int j, double v)
    {
        updated = true;
        consumer.set(i, j, v);
    }

    public void add(int i, int j, double v)
    {
        updated = true;
        consumer.set(i, j, supplier.get(i, j) + v);
    }

    public void sub(int i, int j, double v)
    {
        add(i, j, -v);
    }

    public void mul(int i, int j, double v)
    {
        updated = true;
        consumer.set(i, j, supplier.get(i, j) * v);
    }

    public void div(int i, int j, double v)
    {
        mul(i, j, 1.0 / v);
    }

    public void scalarMultiply(double c)
    {
        updated = true;
        int m = rows.getAsInt();
        int n = cols.getAsInt();
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                consumer.set(i, j, c * supplier.get(i, j));
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        int m = rows.getAsInt();
        int n = cols.getAsInt();
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                sb.append(supplier.get(i, j)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public Matrix multiply(double c)
    {
        Matrix clone = clone();
        clone.scalarMultiply(c);
        return clone;
    }

    public static Matrix multiply(double c, Matrix m)
    {
        Matrix clone = m.clone();
        clone.scalarMultiply(c);
        return clone;
    }

    public boolean isSquare()
    {
        return rows.getAsInt() == cols.getAsInt();
    }

    public boolean isEmpty()
    {
        return rows.getAsInt() == 0 && cols.getAsInt() == 0;
    }

    public Matrix add(Matrix m)
    {
        return add(this, m);
    }

    public Matrix transpose()
    {
        int m = rows();
        int n = colums();
        ItemSupplier s = supplier;
        Matrix tr = getInstance(n, m);
        ItemConsumer c = tr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                c.set(j, i, s.get(i, j));
            }
        }
        return tr;
    }

    public void swapRows(int r1, int r2)
    {
        int n = colums();
        ItemSupplier s = supplier;
        ItemConsumer c = consumer;
        for (int j = 0; j < n; j++)
        {
            double v = s.get(r1, j);
            c.set(r1, j, s.get(r2, j));
            c.set(r2, j, v);
        }
    }

    public void swapRows(int r1, int r2, double[] tmp)
    {
        swapRows(r1, r2);
    }

    public double determinant()
    {
        Matrix A = clone();
        int[] P = new int[rows() + 1];
        lupDecompose(A, 0.001, P);
        return lupDeterminant(A, P);
    }

    public void solve(double[] b, double[] x)
    {
        Matrix A = clone();
        int[] P = new int[rows() + 1];
        lupDecompose(A, 0.001, P);
        lupSolve(A, P, b, x);
    }

    public Matrix invert()
    {
        Matrix A = clone();
        Matrix IA = getInstance(rows(), colums());
        int[] P = new int[rows() + 1];
        lupDecompose(A, 0.001, P);
        lupInvert(A, P, IA);
        return IA;
    }

    private static void lupDecompose(Matrix A, double Tol, int[] P)
    {
        if (!A.isSquare())
        {
            throw new IllegalArgumentException("not square");
        }
        ItemSupplier As = A.supplier;
        int N = A.colums();
        int i, j, k, imax;
        double maxA, absA;
        double[] tmp = new double[N];

        for (i = 0; i <= N; i++)
        {
            P[i] = i; //Unit permutation matrix, P[N] initialized with N
        }
        for (i = 0; i < N; i++)
        {
            maxA = 0.0;
            imax = i;

            for (k = i; k < N; k++)
            {
                if ((absA = Math.abs(As.get(k, i))) > maxA)
                {
                    maxA = absA;
                    imax = k;
                }
            }

            if (maxA < Tol)
            {
                throw new IllegalArgumentException("failure, matrix is degenerate");
            }
            if (imax != i)
            {
                //pivoting P
                j = P[i];
                P[i] = P[imax];
                P[imax] = j;

                //pivoting rows of A
                A.swapRows(i, imax, tmp);

                //counting pivots starting from N (for determinant)
                P[N]++;
            }

            for (j = i + 1; j < N; j++)
            {
                A.div(j, i, As.get(i, i));

                for (k = i + 1; k < N; k++)
                {
                    A.sub(j, k, As.get(j, i) * As.get(i, k));
                }
            }
        }

    }

    private void lupSolve(Matrix A, int[] P, double[] b, double[] x)
    {
        ItemSupplier As = A.supplier;
        int N = A.colums();
        for (int i = 0; i < N; i++)
        {
            x[i] = b[P[i]];

            for (int k = 0; k < i; k++)
            {
                x[i] -= As.get(i, k) * x[k];
            }
        }

        for (int i = N - 1; i >= 0; i--)
        {
            for (int k = i + 1; k < N; k++)
            {
                x[i] -= As.get(i, k) * x[k];
            }

            x[i] = x[i] / As.get(i, i);
        }
    }

    private void lupInvert(Matrix A, int[] P, Matrix IA)
    {
        ItemSupplier As = A.supplier;
        ItemSupplier IAs = IA.supplier;
        ItemConsumer IAc = IA.consumer;
        int N = A.colums();

        for (int j = 0; j < N; j++)
        {
            for (int i = 0; i < N; i++)
            {
                if (P[i] == j)
                {
                    IAc.set(i, j, 1.0);
                }
                else
                {
                    IAc.set(i, j, 0.0);
                }

                for (int k = 0; k < i; k++)
                {
                    IA.sub(i, j, As.get(i, k) * IAs.get(k, j));
                }
            }

            for (int i = N - 1; i >= 0; i--)
            {
                for (int k = i + 1; k < N; k++)
                {
                    IA.sub(i, j, As.get(i, k) * IAs.get(k, j));
                }

                IAc.set(i, j, IAs.get(i, j) / As.get(i, i));
            }
        }
    }

    private static double lupDeterminant(Matrix A, int[] P)
    {
        ItemSupplier As = A.supplier;
        int N = A.colums();

        double det = As.get(0, 0);

        for (int i = 1; i < N; i++)
        {
            det *= As.get(i, i);
        }

        if ((P[N] - N) % 2 == 0)
        {
            return det;
        }
        else
        {
            return -det;
        }
    }

    @Override
    public Matrix clone()
    {
        try
        {
            return (Matrix) super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Matrix)
        {
            Matrix mt = (Matrix) obj;
            if (mt.colums() != colums())
            {
                return false;
            }
            if (mt.rows() != rows())
            {
                return false;
            }
            int m = rows();
            int n = colums();
            for (int i = 0; i < m; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    if (get(i, j) != mt.get(i, j))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static Matrix getInstance(int rows, int cols)
    {
        return new MatrixImpl(rows, cols);
    }

    public static Matrix getInstance(int rows, double... values)
    {
        if (values.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        return new MatrixImpl(rows, values.length / rows, values);
    }

    public static Matrix add(Matrix m1, Matrix m2)
    {
        if (m1.rows.getAsInt() != m2.rows.getAsInt()
                || m1.cols.getAsInt() != m2.cols.getAsInt())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows.getAsInt();
        int n = m1.cols.getAsInt();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        Matrix mr = Matrix.getInstance(m, n);
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                c.set(i, j, s1.get(i, j) + s2.get(i, j));
            }
        }
        return mr;
    }

    public static Matrix multiply(Matrix m1, Matrix m2)
    {
        if (m1.cols.getAsInt() != m2.rows.getAsInt())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows.getAsInt();
        int n = m1.cols.getAsInt();
        int p = m2.cols.getAsInt();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        Matrix mr = Matrix.getInstance(m, p);
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < p; j++)
            {
                double s = 0;
                for (int r = 0; r < n; r++)
                {
                    s += s1.get(i, r) * s2.get(r, j);
                }
                c.set(i, j, s);
            }
        }
        return mr;
    }

    public static Matrix identity(int n)
    {
        return new Identity(n);
    }

    public static class Identity extends Matrix
    {

        public Identity(int n)
        {
            super(n, n);
            supplier = (i, j) -> i == j ? 1 : 0;
        }

        @Override
        public void add(int i, int j, double v)
        {
            throw new UnsupportedOperationException("not supported");
        }

        @Override
        public void set(int i, int j, double v)
        {
            throw new UnsupportedOperationException("not supported");
        }

        @Override
        public void swapRows(int r1, int r2, double[] tmp)
        {
            throw new UnsupportedOperationException("Not supported");
        }

    }

    public static class MatrixImpl extends Matrix
    {

        private double[] d;

        public MatrixImpl(int rows, int cols)
        {
            this(rows, cols, new double[rows * cols]);
        }

        public MatrixImpl(int rows, int cols, double[] d)
        {
            super(rows, cols);
            this.d = d;
            supplier = (i, j) -> d[cols * i + j];
            consumer = (i, j, v) -> d[cols * i + j] = v;
        }

        @Override
        public Matrix clone()
        {
            return new MatrixImpl(rows.getAsInt(), cols.getAsInt(), Arrays.copyOf(d, d.length));
        }

        @Override
        public void swapRows(int r1, int r2, double[] tmp)
        {
            int col = colums();
            System.arraycopy(d, col * r1, tmp, 0, col);
            System.arraycopy(d, col * r2, d, col * r1, col);
            System.arraycopy(tmp, 0, d, col * r2, col);
        }
    }

    @FunctionalInterface
    protected interface ItemSupplier
    {

        /**
         * Returns get ij of matrix
         *
         * @param i
         * @param j
         * @return
         */
        double get(int i, int j);

        /**
         * Returns ItemSupplier which swaps i and j
         *
         * @return
         */
        default ItemSupplier swap()
        {
            return (i, j) -> get(j, i);
        }
    }

    @FunctionalInterface
    protected interface ItemConsumer
    {

        /**
         * Set ij of matrix
         *
         * @param i
         * @param j
         * @param v
         */
        void set(int i, int j, double v);

        /**
         * Returns ItemConsumer which swaps i and j
         *
         * @return
         */
        default ItemConsumer swap()
        {
            return (i, j, v) -> set(j, i, v);
        }
    }
}
