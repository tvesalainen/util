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

/**
 * Average calculator
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleAverage implements Average
{
    private double sum;
    private double count;
    /**
     * Add number of values each with weight = 1.0
     * @param values 
     */
    public void add(double... values)
    {
        for (double value : values)
        {
            add(value);
        }
    }
    /**
     * Add value with weight = 1.0
     * @param value 
     */
    public void add(double value)
    {
        add(value, 1);
    }
    /**
     * Add value with weight
     * @param value
     * @param weight 
     */
    public void add(double value, double weight)
    {
        sum += value*weight;
        count += weight;
    }
    @Override
    public double average()
    {
        return sum/count;
    }

    @Override
    public double fast()
    {
        return sum/count;
    }
}
