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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractMatrix<T> implements Cloneable, Serializable
{
    
    protected static final long serialVersionUID = 1L;
    protected int rows;
    protected int cols;
    protected Index M;
    protected Object array;
    protected Class<T> cls;

    protected AbstractMatrix(int rows, int cols, Class<T> cls)
    {
        this(rows, Array.newInstance(cls, rows*cols));
    }
    protected AbstractMatrix(int rows, Object array)
    {
        if (!array.getClass().isArray())
        {
            throw new IllegalArgumentException("not array");
        }
        int length = Array.getLength(array);
        if (length % rows != 0)
        {
            throw new IllegalArgumentException("wrong number of items");
        }
        this.rows = rows;
        this.cols = length/rows;
        this.array = array;
        this.cls = (Class<T>) array.getClass().getComponentType();
        this.M = (i, j) -> cols * i + j;
    }

    /**
     * Returns new DoubleMatrix initialized to zeroes.
     * @param rows
     * @param cols
     * @return 
     */
    public static AbstractMatrix getInstance(int rows, int cols, Class<?> cls)
    {
        return new AbstractMatrix(rows, Array.newInstance(cls, rows*cols));
    }
    /**
     * Returns new AbstractMatrix initialized to values.
     * <p>E.g. 2x2 matrix has A00, A01, A10, A11
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
     * Returns number of rows
     * @return
     */
    public int rows()
    {
        return rows;
    }

    /**
     * Returns number of columns
     * @return
     */
    public int columns()
    {
        return cols;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        int m = rows;
        int n = cols;
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
     * @return
     */
    public boolean isSquare()
    {
        return rows == cols;
    }

    /**
     * Returns true if matrix has no rows or columns.
     * @return
     */
    public boolean isEmpty()
    {
        return rows == 0 && cols == 0;
    }

    /**
     * Swaps row r1 and r2 possibly using tmp
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
        hash = 67 * hash + this.rows;
        hash = 67 * hash + this.cols;
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
        if (this.cols != other.cols)
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

    @FunctionalInterface
    protected interface Index
    {
        int at(int i, int j);
    }    
}
