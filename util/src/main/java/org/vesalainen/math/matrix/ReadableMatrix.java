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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface ReadableMatrix
{

    /**
     * Returns number of columns
     *
     * @return
     */
    int columns();

    /**
     * Returns number of rows
     *
     * @return
     */
    int rows();

    default boolean sameDimensions(ReadableMatrix o)
    {
        return rows() == o.rows() && columns() == o.columns();
    }

    default int elements()
    {
        return rows() * columns();
    }
    
}
