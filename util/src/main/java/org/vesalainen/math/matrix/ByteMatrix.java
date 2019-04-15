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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteMatrix extends AbstractMatrix
{
    protected ItemSupplier supplier;
    protected ItemConsumer consumer;

    public ByteMatrix(int rows, int cols)
    {
        this(rows, new byte[rows * cols]);
    }

    public ByteMatrix(int rows, byte[] array)
    {
        super(rows, array);
        supplier = (i, j) -> array[cols * i + j];
        consumer = (i, j, v) -> array[cols * i + j] = (byte)v;
    }
     @Override
    public ByteMatrix clone()
    {
        return new ByteMatrix(rows, (byte[]) copyOf(array, cls));
    }

    /**
     * Swaps row r1 and r2 possibly using tmp
     * @param r1
     * @param r2
     * @param tmp double [columns]
     */
    public void swapRows(int r1, int r2, byte[] tmp)
    {
        super.swapRows(r1, r2, tmp);
    }
    /**
     * Sets item at i,j
     * @param i
     * @param j
     * @param v 
     */
    public void set(int i, int j, int  v)
    {
        consumer.set(i, j, v);
    }
    /**
     * Aij = array[j]
     * @param i
     * @param array
     * @param offset 
     */
    public void setRow(int i, int[] array, int offset)
    {
        int n = cols;
        for (int j=0;j<n;j++)
        {
            set(i, j, array[j+offset]);
        }
    }
    
    /**
     * Returns item at i,j. Starts at 0.
     * @param i
     * @param j
     * @return 
     */
    public int get(int i, int j)
    {
        return supplier.get(i, j);
    }
    @FunctionalInterface
    public interface ItemSupplier
    {

        /**
         * Returns get M of matrix
         *
         * @param i
         * @param j
         * @return
         */
        int get(int i, int j);

    }

    @FunctionalInterface
    protected interface ItemConsumer
    {

        /**
         * Set M of matrix
         *
         * @param i
         * @param j
         * @param v
         */
        void set(int i, int j, int v);

    }
}
