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
package org.vesalainen.math;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Exponential implements MathFunction
{
    private double base;
    private MathFunction inverse;
    private double logBase;

    public Exponential(double base)
    {
        this.base = base;
        this.logBase = Math.log(base);
    }

    @Override
    public MathFunction inverse()
    {
        if (inverse == null)
        {
            inverse = new Logarithm(base);
        }
        return inverse;
    }

    @Override
    public MathFunction antiderivative()
    {
        return (x)->Math.pow(base, x)/logBase;
    }

    @Override
    public MathFunction derivative()
    {
        return (x)->Math.pow(base, x)*logBase;
    }

    @Override
    public double applyAsDouble(double operand)
    {
        return Math.pow(base, operand);
    }
    
}
