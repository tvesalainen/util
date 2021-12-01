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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Objects;
import org.vesalainen.util.ArrayHelp;
import org.vesalainen.util.ArrayHelp.RowComparator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class AbstractMatrix<T> implements Cloneable, Serializable
{

    protected static final long serialVersionUID = 1L;
    private IntProvider rows;
    private IntProvider columns;
    protected Index M;
    protected Object array;
    protected Class<T> cls;

    protected AbstractMatrix(int rows, int cols, Class<T> cls)
    {
        this(rows, cols, Array.newInstance(cls, rows * cols));
    }

    protected AbstractMatrix(int rows, Object array)
    {
        this(rows, rows > 0 ? Array.getLength(array) / rows : 0, array);
    }
    protected AbstractMatrix(int rows, int cols, Object array)
    {
        if (!array.getClass().isArray())
        {
            throw new IllegalArgumentException("not array");
        }
        int length = Array.getLength(array);
        if (length != rows*cols)
        {
            throw new IllegalArgumentException("wrong number of items");
        }
        this.rows = ()->rows;
        this.columns = ()->cols;
        this.array = array;
        this.cls = (Class<T>) array.getClass().getComponentType();
        this.M = (i, j) -> cols * i + j;
    }
    protected AbstractMatrix(AbstractMatrix parent, int startRow, int startCol, int rows, int cols, Object array)
    {
        if (rows != -1)
        {
            this.rows = ()->rows;
        }
        else
        {
            this.rows = ()->parent.rows()-startRow;
        }
        if (cols != -1)
        {
            this.columns = ()->cols;
        }
        else
        {
            this.columns = ()->parent.columns()-startCol;
        }
        this.array = array;
        this.cls = (Class<T>) array.getClass().getComponentType();
        this.M = (i, j) -> parent.at(i+startRow, j+startCol);
    }

    protected AbstractMatrix(AbstractMatrix parent, int rows, int cols, Object array, int... pos)
    {
        if (rows != -1)
        {
            if (cols != -1)
            {
                checkPositions(pos, rows+cols);
                this.columns = ()->cols;
                this.M = (i, j) -> parent.at(pos[i], pos[rows+j]);
            }
            else
            {
                checkPositions(pos, rows);
                this.columns = ()->parent.columns();
                this.M = (i, j) -> parent.at(pos[i], j);
            }
            this.rows = ()->rows;
        }
        else
        {
            if (cols != -1)
            {
                checkPositions(pos, cols);
                this.columns = ()->cols;
                this.M = (i, j) -> parent.at(i, pos[j]);
            }
            else
            {
                checkPositions(pos, 0);
                this.columns = ()->parent.columns();
                this.M = (i, j) -> parent.at(i, j);
            }
            this.rows = ()->parent.rows();
        }
        this.array = array;
        this.cls = (Class<T>) array.getClass().getComponentType();
    }
    
    public void setReshape(AbstractMatrix m)
    {
        if (Array.getLength(array) < Array.getLength(m.array))
        {
            array = Array.newInstance(cls, m.elements());
        }
        System.arraycopy(m.array, 0, array, 0, m.elements());

        this.rows = m::rows;
        this.columns = m::columns;
        this.M = (i, j) -> columns.getAsInt() * i + j;
    }
    public void reshape(int rows, int cols)
    {
        reshape(rows, cols, false);
    }
    public void reshape(int rows, int cols, boolean save)
    {
        if (Array.getLength(array) < rows * cols)
        {
            Object d = Array.newInstance(cls, rows * cols);

            if (save)
            {
                System.arraycopy(array, 0, d, 0, elements());
            }

            this.array = d;
        }

        this.rows = ()->rows;
        this.columns = ()->cols;
        this.M = (i, j) -> cols * i + j;
    }
    public void sort(RowComparator comp)
    {
        ArrayHelp.sort(array, columns.getAsInt(), comp);
    }
    /**
     * Copies row1 from a to row2 in b
     * @param a
     * @param row1
     * @param b
     * @param row2 
     */
    public static void copyRow(AbstractMatrix a, int row1, AbstractMatrix b, int row2)
    {
        if (a.columns != b.columns)
        {
            throw new IllegalArgumentException("cols differ");
        }
        int cls = a.columns.getAsInt();
        System.arraycopy(a.array, row1*cls, b.array, row2*cls, cls);
    }
    public boolean sameDimensions(AbstractMatrix o)
    {
        return rows() == o.rows() && columns() == o.columns();
    }
    /**
     * Returns new DoubleMatrix initialized to zeroes.
     *
     * @param rows
     * @param cols
     * @return
     */
    public static AbstractMatrix getInstance(int rows, int cols, Class<?> cls)
    {
        return new AbstractMatrix(rows, Array.newInstance(cls, rows * cols));
    }

    /**
     * Returns new AbstractMatrix initialized to values.
     * <p>
     * E.g. 2x2 matrix has A00, A01, A10, A11
     *
     * @param rows Number of rows
     * @param values Values row by row
     * @return
     */
    public static AbstractMatrix getInstance(int rows, Object values, Class<?> cls)
    {
        int length = Array.getLength(values);
        if (length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        return new AbstractMatrix(rows, copyOf(values, cls));
    }

    protected static Object copyOf(Object arr, Class<?> cls)
    {
        int length = Array.getLength(arr);
        Object na = Array.newInstance(cls, length);
        System.arraycopy(arr, 0, na, 0, length);
        return na;
    }

    @Override
    public AbstractMatrix clone()
    {
        try
        {
            return (AbstractMatrix) super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new IllegalArgumentException();
        }
    }
    /**
     * Returns index to data array
     * @param i
     * @param j
     * @return 
     */
    public int at(int i, int j)
    {
        return M.at(i, j);
    }
    
    @Deprecated
    public int getNumRows()
    {
        return rows();
    }

    @Deprecated
    public int getNumCols()
    {
        return columns();
    }

    @Deprecated
    public int getNumElements()
    {
        return elements();
    }

    public int elements()
    {
        return rows.getAsInt() * columns.getAsInt();
    }
    /**
     * Returns number of rows
     *
     * @return
     */
    public int rows()
    {
        return rows.getAsInt();
    }

    /**
     * Returns number of columns
     *
     * @return
     */
    public int columns()
    {
        return columns.getAsInt();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        int m = rows.getAsInt();
        int n = columns.getAsInt();
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                sb.append(Array.get(array, M.at(i, j))).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns true if matrix is square.
     *
     * @return
     */
    public boolean isSquare()
    {
        return rows() == columns();
    }

    /**
     * Returns true if matrix has no rows or columns.
     *
     * @return
     */
    public boolean isEmpty()
    {
        return rows.getAsInt() == 0 || columns.getAsInt() == 0;
    }

    /**
     * Swaps row r1 and r2 possibly using tmp
     *
     * @param r1
     * @param r2
     * @param tmp double [columns]
     */
    public void swapRows(int r1, int r2, Object tmp)
    {
        int col = columns();
        System.arraycopy(array, col * r1, tmp, 0, col);
        System.arraycopy(array, col * r2, array, col * r1, col);
        System.arraycopy(tmp, 0, array, col * r2, col);
    }

    /**
     * Returns new DoubleMatrix which is transpose of this.
     *
     * @return
     */
    public AbstractMatrix transpose()
    {
        int m = rows();
        int n = columns();
        AbstractMatrix tr = getInstance(n, m, cls);
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++)
            {
                Array.set(tr.array, M.at(j, i), Array.get(array, M.at(i, j)));
            }
        }
        return tr;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + this.rows.getAsInt();
        hash = 67 * hash + this.columns.getAsInt();
        hash = 67 * hash + Objects.hashCode(this.array);
        hash = 67 * hash + Objects.hashCode(this.cls);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AbstractMatrix<?> other = (AbstractMatrix<?>) obj;
        if (this.rows != other.rows)
        {
            return false;
        }
        if (this.columns != other.columns)
        {
            return false;
        }
        if (!Objects.equals(this.array, other.array))
        {
            return false;
        }
        if (!Objects.equals(this.cls, other.cls))
        {
            return false;
        }
        return true;
    }

    private void checkPositions(int[] positions, int len)
    {
        if (positions.length != len)
        {
            throw new IllegalArgumentException("positions array length wrong");
        }
    }

    @FunctionalInterface
    protected interface Index extends Serializable
    {

        int at(int i, int j);
    }
    @FunctionalInterface
    protected interface IntProvider extends Serializable
    {

        int getAsInt();
    }
}
