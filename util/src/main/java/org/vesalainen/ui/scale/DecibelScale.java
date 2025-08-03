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

import org.vesalainen.math.Logarithm;
import org.vesalainen.math.MathFunction;

/**
 * PercentScale is a BasicScale for showing percentage of something.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DecibelScale extends MergeScale
{
    /**
     * Creates PercentScale with 1.0 multiplier and % unit
     * @param reference 
     */
    public DecibelScale(double reference)
    {
        this(1, reference, "dB");
    }
    /**
     * Creates PercentScale with given multiplier and % unit
     * @param reference
     * @param multiplier 
     */
    public DecibelScale(double reference, double multiplier)
    {
        this(1, reference, "dB");
    }
    public DecibelScale(Decibel decibel)
    {
        this(decibel.getExponent(), decibel.getReference(), decibel.getUnit());
    }
    /**
     * Creates PercentScale with given multiplier and given unit
     * @param exponent
     * @param reference
     * @param multiplier
     * @param unit 
     */
    public DecibelScale(int exponent, double reference, String unit)
    {
        super(
                new BasicScale(1, unit, MathFunction.postMultiplier(MathFunction.preMultiplier(new Logarithm(10), 1.0/reference), exponent*10)),
                new BasicScale(3, unit, MathFunction.postMultiplier(MathFunction.preMultiplier(new Logarithm(10), 1.0/reference), exponent*10))
        );
    }

}
