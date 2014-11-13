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
public class Matrices
{
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
