/*
 * Copyright (C) 2014 Timo Vesalainen
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

import org.ejml.data.DenseMatrix64F;

/**
 * Matrices contains utilities for DenseMatrix64F etc.
 * @author Timo Vesalainen
 * @see org.ejml.data.DenseMatrix64F
 */
public final class Matrices
{
    public static boolean removeRow(DenseMatrix64F m, double... row)
    {
        int idx = findRow(m, row);
        if (idx != -1)
        {
            Matrices.removeRowAt(m, idx);
            return true;
        }
        return false;
    }
    /**
     * Returns true if m contains row.
     * @param m
     * @param row
     * @return 
     */
    public static boolean containsRow(DenseMatrix64F m, double... row)
    {
        return findRow(m, row) != -1;
    }
    /**
     * Returns found row number or -1.
     * @param m
     * @param row
     * @return 
     */
    public static int findRow(DenseMatrix64F m, double... row)
    {
        int cols = m.numCols;
        if (row.length != cols)
        {
            throw new IllegalArgumentException("illegal column count");
        }
        double[] d = m.data;
        int rows = m.numRows;
        for (int r=0;r<rows;r++)
        {
            boolean eq=true;
            for (int c=0;c<cols;c++)
            {
                if (d[cols*r+c] != row[c])
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
    public static void setRow(DenseMatrix64F m, int index, double... row)
    {
        int cols = m.numCols;
        if (row.length != cols)
        {
            throw new IllegalArgumentException("illegal column count");
        }
        System.arraycopy(row, 0, m.data, cols*index, row.length);
    }

    public static void addRow(DenseMatrix64F m, double x, double y)
    {
        int cols = m.numCols;
        int rows = m.numRows;
        m.reshape(rows+1, cols, true);
        m.data[cols*rows] = x;
        m.data[cols*rows+1] = y;
    }
    public static void addRow(DenseMatrix64F m, double... row)
    {
        int cols = m.numCols;
        if (row.length != cols)
        {
            throw new IllegalArgumentException("illegal column count");
        }
        int rows = m.numRows;
        m.reshape(rows+1, cols, true);
        System.arraycopy(row, 0, m.data, cols*rows, row.length);
    }
    public static void insertRow(DenseMatrix64F m, int index, double... row)
    {
        int cols = m.numCols;
        if (row.length != cols)
        {
            throw new IllegalArgumentException("illegal column count");
        }
        int rows = m.numRows;
        m.reshape(rows+1, cols, true);
        double[] d = m.data;
        System.arraycopy(d, cols*index, d, cols*(index+1), cols*(rows-index));
        System.arraycopy(row, 0, d, cols*index, row.length);
    }
    public static void removeRowAt(DenseMatrix64F m, int index)
    {
        int cols = m.numCols;
        int rows = m.numRows;
        double[] d = m.data;
        System.arraycopy(d, cols*(index+1), d, cols*index, cols*(rows-index-1));
        m.reshape(rows-1, cols, true);
    }
    /**
     * Removes equal subsequent rows and additionally last row if it is equal to first row.
     * @param matrix 
     */
    public static void removeEqualRows(DenseMatrix64F matrix)
    {
        int rows = matrix.numRows;
        if (rows < 2)
        {
            return;
        }
        double[] d = matrix.data;
        int cols = matrix.numCols;
        int left = rows-1;
        int delta = 0;
        for (int i=0;i<left;i++)
        {
            int j=i;
            for (;j<left && eq(d, i, j+1, cols);j++);
            if (i != j)
            {
                int cnt = j-i;
                System.arraycopy(d, cols*j, d, cols*i, cols*(left-j+1));
                left -= cnt;
                delta += cnt;
            }
        }
        if (eq(d, 0, rows-1, cols))
        {
            delta++;
        }
        if (delta > 0)
        {
            matrix.reshape(rows-delta, cols, true);
        }
    }
    private static boolean eq(double[] d, int i1, int i2, int cols)
    {
        for (int ii=0;ii<cols;ii++)
        {
            if (d[cols*i1+ii] != d[cols*i2+ii])
            {
                return false;
            }
        }
        return true;
    }
    /**
     * MatrixSort is able to sort matrix rows when matrix is stored in one dimensional 
     * array as in DenseMatrix64F.
     * 
     * <p>Sort is using Quick Sort algorithm.
     * @param matrix
     * @param comparator 
     */
    public static void sort(DenseMatrix64F matrix, RowComparator comparator)
    {
        int len = matrix.numCols;
        quickSort(matrix.data, 0, matrix.numRows-1, len, comparator, new double[len], new double[len]);
    }
    /**
     * Sorts rows in 1D array in ascending order using given comparator.
     * @param data
     * @param rowLength
     * @param comparator 
     */
    public static void sort(double[] data, int rowLength, RowComparator comparator)
    {
        quickSort(data, 0, (data.length - 1)/rowLength, rowLength, comparator, new double[rowLength], new double[rowLength]);
    }

    private static void quickSort(double[] arr, int left, int right, int len, RowComparator c, double[] pivot, double[] tmp)
    {
        int i = left, j = right;
        System.arraycopy(arr, ((left + right) / 2)*len, pivot, 0, len);

        /* partition */
        while (i <= j)
        {
            while (c.compare(arr, i, pivot, len) < 0)
            {
                i++;
            }
            while (c.compare(arr, j, pivot, len) > 0)
            {
                j--;
            }
            if (i <= j)
            {
                if (i != j)
                {
                    System.arraycopy(arr, i*len, tmp, 0, len);
                    System.arraycopy(arr, j*len, arr, i*len, len);
                    System.arraycopy(tmp, 0, arr, j*len, len);
                }
                i++;
                j--;
            }
        };

        /* recursion */
        if (left < j)
        {
            quickSort(arr, left, j, len, c, pivot, tmp);
        }
        if (i < right)
        {
            quickSort(arr, i, right, len, c, pivot, tmp);
        }
    }

    public interface RowComparator
    {
        /**
         * Compares sub array of data to pivot array.
         * 
         * <p>Returns -1 if data[row*len,...,(row+1)*len-1] &lt; pivot.
         * <p>Returns 1 if data[row*len,...,(row+1)*len-1] &gt; pivot.
         * <p>Returns 0 if data[row*len,...,(row+1)*len-1] == pivot.
         * @param data
         * @param row
         * @param pivot
         * @param len
         * @return 
         */
        int compare(double[] data, int row, double[] pivot, int len);
    }
}
