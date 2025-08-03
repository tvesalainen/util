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

import java.lang.reflect.Array;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Matrix<T> extends AbstractMatrix
{
    protected ItemSupplier<T> supplier;
    protected ItemConsumer<T> consumer;

    public Matrix(int rows, int cols, Class cls)
    {
        this(rows, (T[])Array.newInstance(cls, rows*cols));
    }

    public Matrix(int rows, T[] array)
    {
        super(rows, array);
        supplier = (i, j) -> array[columns() * i + j];
        consumer = (i, j, v) -> array[columns() * i + j] = v;
    }
    
    @Override
    public Matrix clone()
    {
        return new Matrix(rows(), (T[]) copyOf(array(), cls));
    }

    /**
     * Swaps row r1 and r2 possibly using tmp
     * @param r1
     * @param r2
     * @param tmp double [columns]
     */
    public void swapRows(int r1, int r2, T[] tmp)
    {
        super.swapRows(r1, r2, tmp);
    }
    /**
     * Returns item at i,j. Starts at 0.
     * @param i
     * @param j
     * @return 
     */
    public T get(int i, int j)
    {
        return supplier.get(i, j);
    }
    /**
     * Assign matrix A items starting at i,j
     * @param i
     * @param j
     * @param B 
     */
    public void set(int i, int j, Matrix<T> B)
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
    public void set(int i, int j, T v)
    {
        consumer.set(i, j, v);
    }
    /**
     * Aij = array[j]
     * @param i
     * @param array
     * @param offset 
     */
    public void setRow(int i, T[] array, int offset)
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
    public void getRow(int i, T[] array, int offset)
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
    public void getColumn(int j, T[] array, int offset)
    {
        int m = rows();
        for (int i=0;i<m;i++)
        {
            array[i+offset] = get(i, j);
        }
    }

    @Override
    public Matrix<T> transpose()
    {
        return (Matrix<T>) super.transpose();
    }

    @FunctionalInterface
    public interface ItemSupplier<T>
    {

        /**
         * Returns get M of matrix
         *
         * @param i
         * @param j
         * @return
         */
        T get(int i, int j);

    }

    @FunctionalInterface
    protected interface ItemConsumer<T>
    {

        /**
         * Set M of matrix
         *
         * @param i
         * @param j
         * @param v
         */
        void set(int i, int j, T v);

    }
}
