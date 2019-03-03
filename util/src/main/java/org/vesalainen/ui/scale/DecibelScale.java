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
 * PercentScale is a BasicScale for showing percentage of something.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DecibelScale extends BasicScale
{
    /**
     * Creates PercentScale with 1.0 multiplier and % unit
     * @param reference 
     */
    public DecibelScale(double reference)
    {
        this(1, reference, 1, "dB");
    }
    /**
     * Creates PercentScale with given multiplier and % unit
     * @param reference
     * @param multiplier 
     */
    public DecibelScale(double reference, double multiplier)
    {
        this(1, reference, multiplier, "dB");
    }
    public DecibelScale(Decibel decibel)
    {
        this(decibel, 1);
    }
    public DecibelScale(Decibel decibel, double multiplier)
    {
        this(decibel.getExponent(), decibel.getReference(), multiplier, decibel.getUnit());
    }
    /**
     * Creates PercentScale with given multiplier and given unit
     * @param exponent
     * @param reference
     * @param multiplier
     * @param unit 
     */
    public DecibelScale(int exponent, double reference, double multiplier, String unit)
    {
        super(multiplier, unit, (d)->exponent*10*Math.log10(d/reference));
    }

}
