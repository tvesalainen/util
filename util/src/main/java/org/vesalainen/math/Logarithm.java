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

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Logarithm implements MathFunction
{
    /**
     * Natural logarithm
     */
    public static final Logarithm LOG = new Logarithm(Math.E);
    /**
     * 10 base logarithm
     */
    public static final Logarithm LOG10 = new Logarithm(10);
    private final DoubleUnaryOperator log;
    private MathFunction inv;
    private final DoubleUnaryOperator der;
    private final DoubleUnaryOperator ader;
    private final double base;
    /**
     * Creates a Logarithm with base
     * @param base 
     */
    public Logarithm(double base)
    {
        this.base = base;
        double lb = Math.log(base);
        if (base == Math.E)
        {
            log = Math::log;
            der = (x)->1.0/x;
            ader = (x)->x*Math.log(x)-x;
        }
        else
        {
            if (base == 10.0)
            {
                log = Math::log10;
            }
            else
            {
                log = (double x)->Math.log(x)/lb;
            }
            der = (x)->1.0/(x*lb);
            ader = (x)->(x*Math.log(x)-x)/lb;
        }
    }
    @Override
    public double applyAsDouble(double operand)
    {
        if (operand <= 0.0)
        {
            throw new IllegalArgumentException("not defined <= 0");
        }
        return log.applyAsDouble(operand);
    }

    @Override
    public MathFunction inverse()
    {
        if (inv == null)
        {
            inv = new Exponential(base);
        }
        return inv;
    }

    @Override
    public MathFunction antiderivative()
    {
        return (x)->ader.applyAsDouble(x);
    }

    @Override
    public MathFunction derivative()
    {
        return (x)->der.applyAsDouble(x);
    }
    
}
