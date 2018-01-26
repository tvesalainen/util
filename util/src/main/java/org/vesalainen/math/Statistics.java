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
package org.vesalainen.math;

import org.vesalainen.util.DoubleReference;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Statistics
{
    public static final double meanAbsoluteError(XYSamples samples, final XYModel model)
    {
        final DoubleReference sum = new DoubleReference(0);
        samples.forEach((double x,double y)->sum.add(Math.abs(y-model.getY(x))));
        return Math.sqrt(sum.getValue()/samples.getCount());    
    }
    public static final double rootMeanSquareError(XYSamples samples, final XYModel model)
    {
        final DoubleReference sum = new DoubleReference(0);
        samples.forEach((double x,double y)->sum.add(square(y-model.getY(x))));
        return Math.sqrt(sum.getValue()/samples.getCount());    
    }
    private static double square(double v)
    {
        return v*v;
    }
}
