/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.solver;

import java.util.Random;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RandomVariable extends AbstractVariable
{
    private Random random;
    private double range;
    
    public RandomVariable(double start, double radius)
    {
        this(start, start-radius, start+radius);
    }

    public RandomVariable(double start, double min, double max)
    {
        this(start, min, max, System.currentTimeMillis());
    }
    public RandomVariable(double start, double min, double max, long seed)
    {
        super(start, min, max);
        this.random = new Random(seed);
        this.range = max-min;
    }

    @Override
    protected double newValue(double newSum)
    {
        return range*random.nextDouble()+min;
    }
    
}
