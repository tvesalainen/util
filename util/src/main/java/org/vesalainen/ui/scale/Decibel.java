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
package org.vesalainen.ui.scale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum Decibel
{
    /**
     * dBm one milliwatt (1 mW)
     */
    DBM(1, 0.001, "dBm"),
    /**
     * dBW one watt (1 W)
     */
    DBW(1, 1, "dBW"),
    /**
     * dBV one volt (1 V)
     */
    DBV(2, 1, "dBV"),
    /**
     * dBμV one microvolt (1 μV)
     */
    DBUV(2, 0.000001, "dBμV")
    ;
    private int exponent;
    private double reference;
    private String unit;

    private Decibel(int exponent, double reference, String unit)
    {
        this.exponent = exponent;
        this.reference = reference;
        this.unit = unit;
    }

    public int getExponent()
    {
        return exponent;
    }

    public double getReference()
    {
        return reference;
    }

    public String getUnit()
    {
        return unit;
    }
    
}
