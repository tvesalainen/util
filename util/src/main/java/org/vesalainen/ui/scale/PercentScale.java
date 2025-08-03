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

import org.vesalainen.math.MathFunction;

/**
 * PercentScale is a BasicScale for showing percentage of something.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PercentScale extends MergeScale
{
    /**
     * Creates PercentScale with 1.0 multiplier and % unit
     * @param of 
     */
    public PercentScale(double of)
    {
        this(of, "%");
    }
    /**
     * Creates PercentScale with given multiplier and given unit
     * @param of
     * @param multiplier
     * @param unit 
     */
    public PercentScale(double of, String unit)
    {
        super(
                new BasicScale(1, unit, MathFunction.postMultiplier(MathFunction.preMultiplier(MathFunction.IDENTITY, 1.0/of), 100)),
                new BasicScale(5, unit, MathFunction.postMultiplier(MathFunction.preMultiplier(MathFunction.IDENTITY, 1.0/of), 100))
        );
    }

}
