/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.lang.reflect.Array;

/**
 * <p> if grid is boxed x and y values must stay within their width and height
 boundaries. If grid is not boxed x outside width boundary will flow to next 
 * or previous line as long as resulting coordinates are in grib.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleArrayGrid<T> extends AbstractArrayGrid<T>
{
    protected T[] array;

    public SimpleArrayGrid(T[] array, int width)
    {
        this(array, width, false);
    }
    public SimpleArrayGrid(T[] array, int width, boolean boxed)
    {
        this(array, width, array.length/width, 0, array.length, boxed);
    }

    public SimpleArrayGrid(int width, int heigth, int offset)
    {
        this(width, heigth, offset, false);
    }
    public SimpleArrayGrid(int width, int heigth, int offset, boolean boxed)
    {
        this(null, width, heigth, offset, width*heigth, boxed);
    }

    public SimpleArrayGrid(T[] array, int width, int height, int offset, int length, boolean boxed)
    {
        super(width, height, offset, length, boxed);
        this.array = array;
    }

    /**
     * Returns SimpleArrayGrid view to the same data with possibly different offset
 and/or width
     * @param offset
     * @param width
     * @return
     */
    public SimpleArrayGrid view(int offset, int width)
    {
        return new SimpleArrayGrid(array, width, height, this.offset + offset, length, boxed);
    }

    public T[] getArray()
    {
        return array;
    }

    @Override
    protected void setColor(int position, T color)
    {
        position += offset;
        checkPosition(position);
        if (array == null)
        {
            if (color == null)
            {
                return;
            }
            array = (T[]) Array.newInstance(color.getClass(), length);
        }
        array[position] = color;
    }

    @Override
    protected T getColor(int position)
    {
        position += offset;
        if (array == null || position < 0 || position > length)
        {
            return null;
        }
        return array[position];
    }
}
