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

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractArrayGrid<T> implements Grid<T>
{
    
    protected int width;
    protected int height;
    protected int offset;
    protected int length;
    protected boolean boxed;

    public AbstractArrayGrid(int width, int height, int offset, int length, boolean boxed)
    {
        this.width = width;
        this.height = height;
        this.offset = offset;
        this.length = length;
        this.boxed = boxed;
    }

    /**
     * Returns SimpleArrayGrid view to the same data with possibly different offset
 and/or width
     * @param offset
     * @param width
     * @return
     */
    public abstract SimpleArrayGrid view(int offset, int width);
    
    @Override
    public boolean hit(int x, int y, T color)
    {
        if (!inBox(x, y))
        {
            return false;
        }
        return hit(position(x, y), color);
    }

    public boolean hit(int position, T color)
    {
        if (position < 0 || position >= length || !inBox(position))
        {
            return false;
        }
        T c = getColor(position);
        return Objects.equals(c, color);
    }

    protected int line(int position)
    {
        return position / width;
    }

    protected int column(int position)
    {
        return position % width;
    }

    protected int position(int x, int y)
    {
        return y * width + x;
    }

    @Override
    public void setColor(int x, int y, T color)
    {
        setColor(position(x, y), color);
    }

    @Override
    public T getColor(int x, int y)
    {
        if (!inBox(x, y))
        {
            return null;
        }
        return getColor(position(x, y));
    }

    protected boolean inBox(int position)
    {
        return inBox(column(position), line(position));
    }

    protected boolean inBox(int x, int y)
    {
        return !boxed || (x >= 0 && x < width && y >= 0 && y < height);
    }

    protected abstract void setColor(int position, T color);
    protected abstract T getColor(int position);

    @Override
    public int width()
    {
        return width;
    }

    @Override
    public int height()
    {
        return height;
    }

    public int offset()
    {
        return offset;
    }

    protected void checkPosition(int position)
    {
        if (position < 0 || position > length)
        {
            throw new IllegalArgumentException("illegal position " + position);
        }
    }
    
}
