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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractVariable implements Variable
{
    
    protected double value;
    protected final double min;
    protected final double max;

    public AbstractVariable(double start, double min, double max)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        if (start > max || start < min)
        {
            throw new IllegalArgumentException("start out of (min - max)");
        }
        this.value = start;
        this.min = min;
        this.max = max;
    }

    @Override
    public void update(double newSum)
    {
        value = newValue(newSum);
        if (value > max || value < min)
        {
            throw new IllegalArgumentException(value+"  out of range "+min+" - "+max);
        }
    }

    protected abstract double newValue(double newSum);
    
    @Override
    public double getAsDouble()
    {
        return value;
    }
    
}
