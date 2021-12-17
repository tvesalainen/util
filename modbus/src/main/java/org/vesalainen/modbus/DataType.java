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
package org.vesalainen.modbus;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum DataType
{
    SHORT(1),
    USHORT(1),
    INT(2),
    UINT(2),
    STRING(-1)
    ;
    private int words;
    private DataType(int words)
    {
        this.words = words;
    }

    public int getWords()
    {
        return words;
    }

    
}
