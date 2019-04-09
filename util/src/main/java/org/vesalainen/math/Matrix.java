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

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.IntSupplier;

/**
 * A Matrix. Uses LUP decomposing for invert, solve and determinant. Call 
 * decompose before calling these methods. Decompose tells if matrix is invertible.
 * No use to call determinant for that.
 * <p>Note! Row and column numbers start with 0.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Matrix implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1L;

    protected IntSupplier rows;
    protected IntSupplier cols;
    protected ItemSupplier supplier;
    protected ItemConsumer consumer;
    private Matrix A;
    private int[] P;

    protected Matrix(int rows, int cols)
    {
        this(() -> rows, () -> cols);
    }

    protected Matrix(IntSupplier rows, IntSupplier cols)
    {
        this.rows = rows;
        this.cols = cols;
    }
    /**
     * Returns number of rows
     * @return 
     */
    public int rows()
    {
        return rows.getAsInt();
    }
    /**
     * Returns number of columns
     * @return 
     */
    public int columns()
    {
        return cols.getAsInt();
    }
    /**
     * Returns item at i,j. Starts at 0.
     * @param i
     * @param j
     * @return 
     */
    public double get(int i, int j)
    {
        return supplier.get(i, j);
    }
    /**
     * Assign matrix A items starting at i,j
     * @param i
     * @param j
     * @param B 
     */
    public void set(int i, int j, Matrix B)
    {
        int m = B.rows();
        int n = B.columns();
        for (int ii = 0; ii < m; ii++)
        {
            for (int jj = 0; jj < n; jj++)
            {
                set(i+ii, j+jj, B.get(ii, jj));
            }
        }
    }
    /**
     * Sets item at i,j
     * @param i
     * @param j
     * @param v 
     */
    public void set(int i, int j, double v)
    {
        consumer.set(i, j, v);
    }
    /**
     * Aij = array[j]
     * @param i
     * @param array
     * @param offset 
     */
    public void setRow(int i, double[] array, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            set(i, j, array[j+offset]);
        }
    }
    /**
     * array[j] = Aij
     * @param i
     * @param array
     * @param offset 
     */
    public void getRow(int i, double[] array, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            array[j+offset] = get(i, j);
        }
    }
    /**
     * array[i] = Aij
     * @param j
     * @param array
     * @param offset 
     */
    public void getColumn(int j, double[] array, int offset)
    {
        int m = rows.getAsInt();
        for (int i=0;i<m;i++)
        {
            array[i+offset] = get(i, j);
        }
    }
    /**
     * Aij += array[j]
     * @param i
     * @param arr
     * @param offset 
     */
    public void addRow(int i, double[] arr, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            add(i, j, arr[j+offset]);
        }
    }
    /**
     * Aij -= array[j]
     * @param i
     * @param arr
     * @param offset 
     */
    public void subRow(int i, double[] arr, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            sub(i, j, arr[j+offset]);
        }
    }
    /**
     * Aij *= array[j]
     * @param i
     * @param arr
     * @param offset 
     */
    public void mulRow(int i, double[] arr, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            mul(i, j, arr[j+offset]);
        }
    }
    /**
     * Aij /= array[j]
     * @param i
     * @param arr
     * @param offset 
     */
    public void divRow(int i, double[] arr, int offset)
    {
        int n = cols.getAsInt();
        for (int j=0;j<n;j++)
        {
            div(i, j, arr[j+offset]);
        }
    }
    /**
     * Aij += v
     * @param i
     * @param j
     * @param v 
     */
    public void add(int i, int j, double v)
    {
        consumer.set(i, j, supplier.get(i, j) + v);
    }
    /**
     * Aij -= v
     * @param i
     * @param j
     * @param v 
     */
    public void sub(int i, int j, double v)
    {
        consumer.set(i, j, supplier.get(i, j) - v);
    }
    /**
     * Aij *= v
     * @param i
     * @param j
     * @param v 
     */
    public void mul(int i, int j, double v)
    {
        consumer.set(i, j, supplier.get(i, j) * v);
    }
    /**
     * Aij /= v
     * @param i
     * @param j
     * @param v 
     */
    public void div(int i, int j, double v)
    {
        consumer.set(i, j, supplier.get(i, j) / v);
    }
    /**
     * Scalar multiplies each item with c
     * @param c 
     */
    public void scalarMultiply(double c)
    {
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
    /**
     * Return new Matrix which is copy of this Matrix with each item scalar multiplied with c.
     * @param c
     * @return 
     */
    public Matrix multiply(double c)
    {
        Matrix clone = clone();
        clone.scalarMultiply(c);
        return clone;
    }
    /**
     * Return new Matrix which is copy of given Matrix with each item scalar multiplied with c.
     * @param c
     * @param m
     * @return 
     */
    public static Matrix multiply(double c, Matrix m)
    {
        Matrix clone = m.clone();
        clone.scalarMultiply(c);
        return clone;
    }
    /**
     * Returns true if matrix is square.
     * @return 
     */
    public boolean isSquare()
    {
        return rows.getAsInt() == cols.getAsInt();
    }
    /**
     * Returns true if matrix has no rows or columns.
     * @return 
     */
    public boolean isEmpty()
    {
        return rows.getAsInt() == 0 && cols.getAsInt() == 0;
    }
    /**
     * Returns new Matrix which is this added to m
     * @param m
     * @return 
     */
    public Matrix add(Matrix m)
    {
        return add(this, m);
    }
    /**
     * Returns new Matrix which is transpose of this.
     * @return 
     */
    public Matrix transpose()
    {
        int m = rows();
        int n = columns();
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
    /**
     * Swaps row r1 and r2
     * @param r1
     * @param r2 
     */
    public void swapRows(int r1, int r2)
    {
        int n = columns();
        ItemSupplier s = supplier;
        ItemConsumer c = consumer;
        for (int j = 0; j < n; j++)
        {
            double v = s.get(r1, j);
            c.set(r1, j, s.get(r2, j));
            c.set(r2, j, v);
        }
    }
    /**
     * Swaps row r1 and r2 possibly using tmp
     * @param r1
     * @param r2
     * @param tmp double [columns]
     */
    public void swapRows(int r1, int r2, double[] tmp)
    {
        swapRows(r1, r2);
    }
    /**
     * Return determinant.
     * @return 
     */
    public double determinant()
    {
        if (A == null)
        {
            throw new IllegalArgumentException("decompose() not called");
        }
        return lupDeterminant(A, P);
    }
    /**
     * Solve linear equation Ax = b returning x
     * @param b
     * @return 
     */
    public Matrix solve(Matrix b)
    {
        Matrix x = getInstance(b.rows(), b.columns());
        solve(b, x);
        return x;
    }
    /**
     * Solve linear equation Ax = b
     * @param b
     * @param x 
     */
    public void solve(Matrix b, Matrix x)
    {
        if (A == null)
        {
            throw new IllegalArgumentException("decompose() not called");
        }
        lupSolve(A, P, b, x);
    }
    /**
     * Returns inverted
     * @return 
     */
    public Matrix invert()
    {
        if (A == null)
        {
            throw new IllegalArgumentException("decompose() not called");
        }
        Matrix IA = getInstance(rows(), columns());
        lupInvert(A, P, IA);
        return IA;
    }
    /**
     * Does LUP decomposition. This method is called before solve(), invert() or
     * determinant() and must be called if matrix has changed before calling previous
     * methods.
     */
    public void decompose()
    {
        A = clone();
        P = new int[rows() + 1];
        lupDecompose(A, 0.001, P);
    }
    private static void lupDecompose(Matrix A, double Tol, int[] P)
    {
        if (!A.isSquare())
        {
            throw new IllegalArgumentException("not square");
        }
        ItemSupplier As = A.supplier;
        int N = A.columns();
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

    private void lupSolve(Matrix A, int[] P, Matrix b, Matrix x)
    {
        ItemSupplier As = A.supplier;
        ItemSupplier bs = b.supplier;
        ItemSupplier xs = x.supplier;
        ItemConsumer xc = x.consumer;
        int N = A.rows();
        int M = b.columns();
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < M; j++)
            {
                xc.set(i, j, bs.get(P[i], j));
            }
            for (int k = 0; k < i; k++)
            {
                for (int j = 0; j < M; j++)
                {
                    x.sub(i, j, As.get(i, k) * xs.get(k, j));
                }
            }
        }

        for (int i = N - 1; i >= 0; i--)
        {
            for (int k = i + 1; k < N; k++)
            {
                for (int j = 0; j < M; j++)
                {
                    x.sub(i, j, As.get(i, k) * xs.get(k, j));
                }
            }

            for (int j = 0; j < M; j++)
            {
                x.div(i, j, As.get(i, i));
            }
        }
    }

    private void lupInvert(Matrix A, int[] P, Matrix IA)
    {
        ItemSupplier As = A.supplier;
        ItemSupplier IAs = IA.supplier;
        ItemConsumer IAc = IA.consumer;
        int N = A.columns();

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
        int N = A.columns();

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
    /**
     * Returns independent clone
     * @return 
     */
    @Override
    public abstract Matrix clone();
    /**
     * Returns true if other conforms with this and each item differs less than 2*ulp(v).
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Matrix)
        {
            Matrix mt = (Matrix) other;
            if (mt.columns() != columns())
            {
                return false;
            }
            if (mt.rows() != rows())
            {
                return false;
            }
            int m = rows();
            int n = columns();
            for (int i = 0; i < m; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    double v = get(i, j);
                    double u = 2*Math.ulp(v);
                    double d = v - mt.get(i, j);
                    if (Math.abs(d) > u)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    /**
     * Returns new Matrix initialized to zeroes.
     * @param rows
     * @param cols
     * @return 
     */
    public static Matrix getInstance(int rows, int cols)
    {
        return new MatrixImpl(rows, cols);
    }
    /**
     * Returns new Matrix initialized to values.
     * <p>E.g. 2x2 matrix has A00, A01, A10, A11
     * @param rows Number of rows
     * @param values Values row by row
     * @return 
     */
    public static Matrix getInstance(int rows, double... values)
    {
        if (values.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        return new MatrixImpl(rows, values.length / rows, Arrays.copyOf(values, values.length));
    }
    /**
     * Returns new Matrix initialized by function
     * @param rows
     * @param cols
     * @param s
     * @return 
     */
    public static Matrix getInstance(int rows, int cols, ItemSupplier s)
    {
        Matrix m = new MatrixImpl(rows, cols);
        ItemConsumer c = m.consumer;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                c.set(i, j, s.get(i, j));
            }
        }
        return m;
    }
    /**
     * Returns new Matrix which is m1 added with m2
     * @param m1
     * @param m2
     * @return 
     */
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
    /**
     * Returns new Matrix which is m1 multiplied with m2.
     * @param m1
     * @param m2
     * @return 
     */
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
    /**
     * Returns new identity matrix.
     * @param n
     * @return 
     */
    public static Matrix identity(int n)
    {
        return getInstance(n ,n, (i, j) -> i == j ? 1 : 0);
    }

    protected static class MatrixImpl extends Matrix
    {
        private double[] d;

        protected MatrixImpl(int rows, int cols)
        {
            this(rows, cols, new double[rows * cols]);
        }

        protected MatrixImpl(int rows, int cols, double[] d)
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
            int col = columns();
            System.arraycopy(d, col * r1, tmp, 0, col);
            System.arraycopy(d, col * r2, d, col * r1, col);
            System.arraycopy(tmp, 0, d, col * r2, col);
        }
    }

    @FunctionalInterface
    public interface ItemSupplier
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
