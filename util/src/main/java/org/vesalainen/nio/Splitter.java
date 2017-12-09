/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio;

import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Splitter<T>
{
    private int size;

    public Splitter(int size)
    {
        this.size = size;
    }
    
    public int split(T obj, int start, int length) throws IOException
    {
        int count = 0;
        if (length > 0)
        {
            int end = (start + length) % size;
            if (start < end)
            {
                count = op(obj, start, end);
            }
            else
            {
                if (end > 0)
                {
                    count = op(obj, start, size, 0, end);
                }
                else
                {
                    count = op(obj, start, size);
                }
            }
        }
        return count;
    }

    protected abstract int op(T obj, int position, int limit) throws IOException;

    protected abstract int op(T obj, int position1, int limit1, int position2, int limit2) throws IOException;
    
}
