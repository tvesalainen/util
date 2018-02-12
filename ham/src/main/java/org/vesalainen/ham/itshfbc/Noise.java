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
package org.vesalainen.ham.itshfbc;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum Noise
{
    INDUSTRIAL(140.4),
    RESIDENTIAL(144.7),
    RURAL(150),
    REMOTE(163.6)
    ;
    private double value;

    private Noise(double value)
    {
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }
    
}
