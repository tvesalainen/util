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
package org.vesalainen.math.matrix;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import static java.util.Locale.US;
import java.util.function.IntSupplier;
import org.vesalainen.util.ArrayHelp;

/**
 * A DoubleMatrix. Uses LUP decomposing for invert, solve and determinant. Call 
 * decompose before calling these methods. Decompose tells if matrix is invertible.
 * No use to call determinant for that.
 * <p>Note! Row and column numbers start with 0.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleMatrix extends AbstractMatrix
{
    protected ItemSupplier supplier;
    protected ItemConsumer consumer;
    private DoubleMatrix A;
    private int[] P;

    public DoubleMatrix(int rows, int cols)
    {
        this(rows, cols, new double[rows * cols]);
    }

    public DoubleMatrix(double[][] m)
    {
        this(m.length, ArrayHelp.flatten(m));
    }

    @Deprecated public DoubleMatrix(int rows, int cols, boolean rowMajor, double... d)
    {
        this(rows, cols, Arrays.copyOf(d, rows*cols));
        if (!rowMajor)
        {
            throw new UnsupportedOperationException("rowMajor not supported");
        }
    }
    public DoubleMatrix(int rows, double[] d)
    {
        this(rows, d.length / rows, d);
    }
    public DoubleMatrix(int rows, int cols, double... d)
    {
        super(rows, cols, d);
        supplier = (i, j) -> Array.getDouble(array, cols * i + j);
        consumer = (i, j, v) -> Array.setDouble(array, cols * i + j, v);
    }

    private DoubleMatrix(int origCol, int startRow, int startCol, int rows, int cols, Object array)
    {
        super(origCol, startRow, startCol, rows, cols, array);
    }
    /**
     * Returns view of sub matrix. Returned Matrix shares the same data.
     * Matrix covers the rest of the matrix.
     * @param startRow
     * @param startCol
     * @return 
     */
    public DoubleMatrix getView(int startRow, int startCol)
    {
        return getView(startRow, startCol, rows()-startRow, columns()-startCol);
    }
    /**
     * Returns view of sub matrix. Returned Matrix shares the same data.
     * @param startRow
     * @param startCol
     * @param rows
     * @param cols
     * @return 
     */
    public DoubleMatrix getView(int startRow, int startCol, int rows, int cols)
    {
        if (startRow+rows > rows() || startCol+cols > columns())
        {
            throw new IllegalArgumentException("rows/cols overlap");
        }
        IntSupplier colSup = ()->columns();
        DoubleMatrix m = new DoubleMatrix(colSup.getAsInt(), startRow, startCol, rows, cols, array);
        m.supplier = (i, j) -> Array.getDouble(array, colSup.getAsInt() * (i+startCol) + (j+startRow));
        m.consumer = (i, j, v) -> Array.setDouble(array, colSup.getAsInt() * (i+startCol) + (j+startRow), v);
        return m;
    }
    @Override
    public void reshape(int rows, int cols, boolean save)
    {
        super.reshape(rows, cols, save);
        supplier = (i, j) -> Array.getDouble(array, cols * i + j);
        consumer = (i, j, v) -> Array.setDouble(array, cols * i + j, v);
    }
    public double[] data()
    {
        return (double[]) array;
    }
    /**
     * Return copy of matrix as 2D array.
     * @return 
     */
    public double[][] as2D()
    {
        return ArrayHelp.unFlatten(rows(), (double[]) array);
    }
    @Override
    public DoubleMatrix clone()
    {
        return new DoubleMatrix(rows(), (double[]) copyOf(array, cls));
    }
    /**
     * Access as vector
     * @param i
     * @return 
     */
    public double data(int i)
    {
        if (!(rows() == 1 || columns() == 1))
        {
            throw new IllegalArgumentException();
        }
        return ((double[])array)[i];
    }
    /**
     * Access as vector
     * @param i
     * @param v 
     */
    public void data(int i, double v)
    {
        if (!(rows() == 1 || columns() == 1))
        {
            throw new IllegalArgumentException();
        }
        ((double[])array)[i] = v;
    }
    /**
     * Swaps row r1 and r2 possibly using tmp
     * @param r1
     * @param r2
     * @param tmp double [columns]
     */
    public void swapRows(int r1, int r2, double[] tmp)
    {
        super.swapRows(r1, r2, tmp);
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
    public void zero()
    {
        set(0);
    }
    /**
     * Set all elements to v
     * @param v 
     */
    public void set(double v)
    {
        int m = rows();
        int n = columns();
        ItemConsumer ic = consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                ic.set(i, j, v);
            }
        }
    }
    /**
     * this = B
     * @param B 
     */
    public void set(DoubleMatrix B)
    {
        if (rows() != B.rows() || columns() != B.columns())
        {
            throw new IllegalArgumentException("cols/rows differ");
        }
        set(0, 0, B);
    }
    /**
     * Assign matrix A items starting at i,j
     * @param i
     * @param j
     * @param B 
     */
    public void set(int i, int j, DoubleMatrix B)
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
        int n = columns();
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
        int n = columns();
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
        int m = rows();
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
        int n = columns();
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
        int n = columns();
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
        int n = columns();
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
        int n = columns();
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
    public static void divide(DoubleMatrix a, double v)
    {
        int m = a.rows();
        int n = a.columns();
        ItemConsumer ic = a.consumer;
        ItemSupplier is = a.supplier;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                ic.set(i, j, is.get(i, j) / v);
            }
        }
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
    public static double elementSum(DoubleMatrix a)
    {
        return a.elementSum();
    }
    public double elementSum()
    {
        int m = rows();
        int n = columns();
        ItemSupplier is = supplier;
        double sum = 0;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                sum += is.get(i, j);
            }
        }
        return sum;
    }
    public static void add( double alpha , DoubleMatrix a , double beta , DoubleMatrix b , DoubleMatrix c )
    {
        if (!a.sameDimensions(b))
        {
            throw new IllegalArgumentException("dims differ");
        }
        int m = a.rows();
        int n = a.columns();
        ItemSupplier sa = a.supplier;
        ItemSupplier sb = b.supplier;
        ItemConsumer ic = c.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                ic.set(i, j, alpha*sa.get(i, j)+beta*sb.get(i, j));
            }
        }
    }
    public static double diffNorm(DoubleMatrix a, DoubleMatrix b)
    {
        if (!a.sameDimensions(b))
        {
            throw new IllegalArgumentException("dims differ");
        }
        int m = a.rows();
        int n = a.columns();
        double sum = 0;
        ItemSupplier sa = a.supplier;
        ItemSupplier sb = b.supplier;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                sum += Math.pow(sa.get(i, j)-sb.get(i, j), 2);
            }
        }
        return Math.sqrt(sum);
    }
    public static void scale(double c, DoubleMatrix a)
    {
        a.scalarMultiply(c);
    }
    /**
     * Scalar multiplies each item with c
     * @param c 
     */
    public void scalarMultiply(double c)
    {
        int m = rows();
        int n = columns();
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                consumer.set(i, j, c * supplier.get(i, j));
            }
        }
    }

    /**
     * Return new DoubleMatrix which is copy of this DoubleMatrix with each item scalar multiplied with c.
     * @param c
     * @return 
     */
    public DoubleMatrix multiply(double c)
    {
        DoubleMatrix clone = clone();
        clone.scalarMultiply(c);
        return clone;
    }
    /**
     * Return new DoubleMatrix which is copy of given DoubleMatrix with each item scalar multiplied with c.
     * @param c
     * @param m
     * @return 
     */
    public static DoubleMatrix multiply(double c, DoubleMatrix m)
    {
        DoubleMatrix clone = m.clone();
        clone.scalarMultiply(c);
        return clone;
    }
    /**
     * Returns new DoubleMatrix which is this added to m
     * @param m
     * @return 
     */
    public DoubleMatrix add(DoubleMatrix m)
    {
        return add(this, m);
    }
    public static void multTransB(DoubleMatrix a, DoubleMatrix b, DoubleMatrix c)
    {
        if( a == c || b == c )
        {
            throw new IllegalArgumentException("a or b can be the same matrix as c");
        }
        if( a.columns() != b.columns() ) 
        {
            throw new IllegalArgumentException("The a and b matrices do not have compatible dimensions");
        } 
        if( a.rows() != c.rows() || b.rows() != c.columns() ) 
        {
            throw new IllegalArgumentException("The results matrix does not have the desired dimensions");
        }
        int m = a.rows();
        int n = b.rows();
        int p = b.columns();
        ItemSupplier sa = a.supplier;
        ItemSupplier sb = b.supplier;
        ItemConsumer co = c.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                double sum = 0;
                for (int k=0;k<p;k++)
                {
                    sum += sa.get(i, k)*sb.get(j, k);
                }
                co.set(i, j, sum);
            }
        }
    }
    /**
     * Returns new DoubleMatrix which is transpose of this.
     * @return 
     */
    @Override
    public DoubleMatrix transpose()
    {
        int m = rows();
        int n = columns();
        ItemSupplier s = supplier;
        DoubleMatrix tr = new DoubleMatrix(n, m);
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
     * Return determinant.
     * @return 
     */
    public double determinant()
    {
        if (A == null)
        {
            return permutationDeterminant();
        }
        return lupDeterminant(A, P);
    }
    /**
     * Calculates determinant by using permutations
     * @return 
     */
    public double permutationDeterminant()
    {
        int sign = 1;
        double sum = 0;
        PermutationMatrix pm = PermutationMatrix.getInstance(rows());
        int perms = pm.rows();
        for (int p=0;p<perms;p++)
        {
            double mul = 1;
            for (int i=0;i<rows();i++)
            {
                mul *= get(i, pm.get(p, i));
            }
            sum += sign*mul;
            sign = -sign;
        }
        return sum;
    }
    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned.
     *
     * @param a A matrix that is m by n. Not modified.
     * @param b A matrix that is n by k. Not modified.
     * @param x A matrix that is m by k. Modified.
     *
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve(DoubleMatrix a, DoubleMatrix b, DoubleMatrix x)
    {
        try
        {
            a.decompose();
            a.solve(b, x);
            return true;
        }
        catch (IllegalArgumentException ex)
        {
            return false;
        }
    }
    /**
     * Solve linear equation Ax = b returning x
     * @param b
     * @return 
     */
    public DoubleMatrix solve(DoubleMatrix b)
    {
        DoubleMatrix x = new DoubleMatrix(b.rows(), b.columns());
        solve(b, x);
        return x;
    }
    /**
     * Solve linear equation Ax = b
     * @param b
     * @param x 
     */
    public void solve(DoubleMatrix b, DoubleMatrix x)
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
    public DoubleMatrix invert()
    {
        if (A == null)
        {
            throw new IllegalArgumentException("decompose() not called");
        }
        DoubleMatrix IA = new DoubleMatrix(rows(), columns());
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
        lupDecompose(A, 10e-10, P);
    }
    private static void lupDecompose(DoubleMatrix A, double Tol, int[] P)
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

    private void lupSolve(DoubleMatrix A, int[] P, DoubleMatrix b, DoubleMatrix x)
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

    private void lupInvert(DoubleMatrix A, int[] P, DoubleMatrix IA)
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

    private static double lupDeterminant(DoubleMatrix A, int[] P)
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
     * Returns true if other conforms with this and each item differs less than 2*ulp(v).
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof DoubleMatrix)
        {
            DoubleMatrix mt = (DoubleMatrix) other;
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
     * @deprecated Use initializer
     * Returns new DoubleMatrix initialized to zeroes.
     * @param rows
     * @param cols
     * @return 
     */
    public static DoubleMatrix getInstance(int rows, int cols)
    {
        return new DoubleMatrix(rows, cols);
    }
    /**
     * Returns new DoubleMatrix initialized to values.
     * <p>E.g. 2x2 matrix has A00, A01, A10, A11
     * @param rows Number of rows
     * @param values Values row by row
     * @return 
     */
    public static DoubleMatrix getInstance(int rows, double... values)
    {
        if (rows < 1)
        {
            throw new IllegalArgumentException("rows");
        }
        if (values.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        return new DoubleMatrix(rows, Arrays.copyOf(values, values.length));
    }
    /**
     * Returns new DoubleMatrix initialized by function
     * @param rows
     * @param cols
     * @param s
     * @return 
     */
    public static DoubleMatrix getInstance(int rows, int cols, ItemSupplier s)
    {
        DoubleMatrix m = new DoubleMatrix(rows, cols);
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
     * Returns new DoubleMatrix which is m1 added with m2
     * @param m1
     * @param m2
     * @return 
     */
    public static DoubleMatrix add(DoubleMatrix m1, DoubleMatrix m2)
    {
        DoubleMatrix mr = new DoubleMatrix(m1.rows(), m1.columns());
        add(m1, m2, mr);
        return mr;
    }
    /**
     * c = a + b
     * @param m1
     * @param m2
     * @param mr
     * @return 
     */
    public static void add(DoubleMatrix m1, DoubleMatrix m2, DoubleMatrix mr)
    {
        if (m1.rows() != m2.rows()
                || m1.columns() != m2.columns())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows();
        int n = m1.columns();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                c.set(i, j, s1.get(i, j) + s2.get(i, j));
            }
        }
    }
    /**
     * this = this - m2
     * @param m2 
     */
    public static void subtractEquals(DoubleMatrix m1, DoubleMatrix m2)
    {
        subtract(m1, m2, m1);
    }
    /**
     * Returns this - m2
     * @param m2
     * @return 
     */
    public DoubleMatrix subtract(DoubleMatrix m2)
    {
        return subtract(this, m2);
    }
    /**
     * Returns new DoubleMatrix which is m1 added with m2
     * @param m1
     * @param m2
     * @return 
     */
    public static DoubleMatrix subtract(DoubleMatrix m1, DoubleMatrix m2)
    {
        DoubleMatrix mr = new DoubleMatrix(m1.rows(), m1.columns());
        subtract(m1, m2, mr);
        return mr;
    }
    /**
     * c = a - b
     * @param m1
     * @param m2
     * @param mr 
     */
    public static void subtract(DoubleMatrix m1, DoubleMatrix m2, DoubleMatrix mr)
    {
        if (m1.rows() != m2.rows()
                || m1.columns() != m2.columns())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows();
        int n = m1.columns();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                c.set(i, j, s1.get(i, j) - s2.get(i, j));
            }
        }
    }
    /**
     * Returns new DoubleMatrix which is this multiplied with m.
     * @param m
     * @return 
     */
    public DoubleMatrix multiply(DoubleMatrix m)
    {
        return multiply(this, m);
    }
    /**
     * Returns new DoubleMatrix which is m1 multiplied with m2.
     * @param m1
     * @param m2
     * @return 
     */
    public static DoubleMatrix multiply(DoubleMatrix m1, DoubleMatrix m2)
    {
        if (m1.columns() != m2.rows())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows();
        int n = m1.columns();
        int p = m2.columns();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        DoubleMatrix mr = new DoubleMatrix(m, p);
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
    public static DoubleMatrix identity(int n)
    {
        return getInstance(n ,n, (i, j) -> i == j ? 1 : 0);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        int m = rows();
        int n = columns();
        for (int i = 0; i < m; i++)
        {
            sb.append('\n');
            for (int j = 0; j < n; j++)
            {
                sb.append(String.format(US, "%.3f", Array.getDouble(array, M.at(i, j)))).append(' ');
            }
        }
        return sb.toString();
    }

    @FunctionalInterface
    public interface ItemSupplier extends Serializable
    {

        /**
         * Returns get M of matrix
         *
         * @param i
         * @param j
         * @return
         */
        double get(int i, int j);

    }

    @FunctionalInterface
    protected interface ItemConsumer extends Serializable
    {

        /**
         * Set M of matrix
         *
         * @param i
         * @param j
         * @param v
         */
        void set(int i, int j, double v);

    }
    public boolean removeRow(double... row)
    {
        int idx = findRow(row);
        if (idx != -1)
        {
            removeRowAt(idx);
            return true;
        }
        return false;
    }
    /**
     * Returns true if m contains row.
     * @param row
     * @return 
     */
    public boolean containsRow(double... row)
    {
        return findRow(row) != -1;
    }
    /**
     * Returns found row number or -1.
     * @param row
     * @return 
     */
    public int findRow(double... row)
    {
        if (row.length != columns())
        {
            throw new IllegalArgumentException("illegal column count");
        }
        for (int r=0;r<rows();r++)
        {
            boolean eq=true;
            for (int c=0;c<columns();c++)
            {
                if (get(r, c) != row[c])
                {
                    eq = false;
                    break;
                }
            }
            if (eq)
            {
                return r;
            }
        }
        return -1;
    }
    public void setRow(int index, double... row)
    {
        if (row.length != columns())
        {
            throw new IllegalArgumentException("illegal column count");
        }
        System.arraycopy(row, 0, array, columns()*index, row.length);
    }

    public void addRow(double... row)
    {
        int r = rows();
        if (row.length != columns())
        {
            throw new IllegalArgumentException("illegal column count");
        }
        reshape(rows()+1, columns(), true);
        System.arraycopy(row, 0, array, columns()*r, row.length);
    }
    public void insertRow(int index, double... row)
    {
        int r = rows();
        if (row.length != columns())
        {
            throw new IllegalArgumentException("illegal column count");
        }
        reshape(rows()+1, columns(), true);
        System.arraycopy(array, columns()*index, array, columns()*(index+1), columns()*(r-index));
        System.arraycopy(row, 0, array, columns()*index, row.length);
    }
    public void removeRowAt(int index)
    {
        System.arraycopy(array, columns()*(index+1), array, columns()*index, columns()*(rows()-index-1));
        reshape(rows()-1, columns(), true);
    }
    /**
     * Removes equal subsequent rows and additionally last row if it is equal to first row.
     * @param matrix 
     */
    public void removeEqualRows()
    {
        if (rows() < 2)
        {
            return;
        }
        int left = rows()-1;
        int delta = 0;
        for (int i=0;i<left;i++)
        {
            int j=i;
            for (;j<left && eq(array, i, j+1, columns());j++);
            if (i != j)
            {
                int cnt = j-i;
                System.arraycopy(array, columns()*j, array, columns()*i, columns()*(left-j+1));
                left -= cnt;
                delta += cnt;
            }
        }
        if (eq(array, 0, rows()-1, columns()))
        {
            delta++;
        }
        if (delta > 0)
        {
            reshape(rows()-delta, columns(), true);
        }
    }
    private boolean eq(Object d, int i1, int i2, int cols)
    {
        for (int ii=0;ii<cols;ii++)
        {
            if (!ArrayHelp.rowsEqual(d, cols*i1+ii, cols*i2+ii))
            {
                return false;
            }
        }
        return true;
    }
}
