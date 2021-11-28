/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleMatrixView extends DoubleMatrix
{
    
    protected DoubleMatrixView(IntProvider origRows, IntProvider origCols, int startRow, int startCol, int rows, int cols, Object array)
    {
        super(origRows, origCols, startRow, startCol, rows, cols, array);
    }

    public DoubleMatrixView(IntProvider origRows, IntProvider origCols, int rows, int cols, Object array, int... pos)
    {
        super(origRows, origCols, rows, cols, array, pos);
    }

    @Override
    public void removeEqualRows()
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void removeRowAt(int index)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void insertRow(int index, double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void addRow(double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void setRow(int index, double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public int findRow(double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public boolean containsRow(double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public boolean removeRow(double... row)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void swapRows(int r1, int r2)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void swapRows(int r1, int r2, double[] tmp)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public double[][] as2D()
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public double[] data()
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void reshape(int rows, int cols, boolean save)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void swapRows(int r1, int r2, Object tmp)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void sort(ArrayHelp.RowComparator comp)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void reshape(int rows, int cols)
    {
        throw new UnsupportedOperationException("not supported in view");
    }

    @Override
    public void setReshape(AbstractMatrix m)
    {
        throw new UnsupportedOperationException("not supported in view");
    }
    
}
