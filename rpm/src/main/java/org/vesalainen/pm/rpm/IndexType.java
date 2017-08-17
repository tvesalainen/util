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
package org.vesalainen.pm.rpm;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum IndexType
{
    NULL(-1),
    CHAR(1),
    INT8(1),
    INT16(2),
    INT32(4),
    INT64(-1),
    STRING(-1),
    BIN(1),
    STRING_ARRAY(-1),
    I18NSTRING(-1);
    
    private int size;

    private IndexType(int size)
    {
        this.size = size;
    }

    public int getSize()
    {
        return size;
    }
    
}
