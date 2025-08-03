/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 * @deprecated KISS!!!
 * Recyclable object can be recycled with Recycler.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.util.Recycler
 */
public interface Recyclable
{
    /**
     * Clears objects fields;
     */
    void clear();
    /**
     * Return true if object is recycled. This can be used e.g. in assert to 
     * check that used object is not recycled.
     * @return 
     */
    default boolean isRecycled()
    {
        return Recycler.isRecycled(this);
    }
}
