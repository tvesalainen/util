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
public class LinearVariable extends AbstractVariable
{
    private double sum = Double.MAX_VALUE;
    private double step;
    
    public LinearVariable(double start, double radius)
    {
        this(start, start-radius, start+radius);
    }

    public LinearVariable(double start, double min, double max)
    {
        super(start, min, max);
        this.step = (max - min)/10;
    }

    @Override
    protected double newValue(double newSum)
    {
        if (newSum < sum)
        {
            sum = newSum;
        }
        else
        {
            value -= step;
            step *= -0.5;
        }
        value += step;
        return value;
    }
    
}
