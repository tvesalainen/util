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
public interface MathFunction extends DoubleUnaryOperator
{
    default MathFunction inverse()
    {
        throw new UnsupportedOperationException("inverse not supported");
    }
    default MathFunction integral()
    {
        throw new UnsupportedOperationException("integral not supported");
    }
    default MathFunction derivate()
    {
        return (double x)->
        {
            double d = Math.ulp(x);
            double y = applyAsDouble(x);
            double dy = applyAsDouble(x+d);
            return (dy-y)/d;
        };
    }
    default double integral(double x1, double x2)
    {
        return integral(x1, x2, 60000);
    }
    default double integral(double x1, double x2, int points)
    {
        double delta = (x2-x1)/points;
        double delta2 = delta/2.0;
        double sum = 0;
        double y1 = applyAsDouble(x1);
        double y2;
        for (int ii=1;ii<=points;ii++)
        {
            x1 += delta;
            y2 = applyAsDouble(x1);
            sum += (y1+y2)*delta2;
            y1 = y2;
        }
        return sum;
    }
    default double arc(double x1, double x2)
    {
        return arc(x1, x2, 60000);
    }
    default double arc(double x1, double x2, int points)
    {
        double delta = (x2-x1)/points;
        double sum = 0;
        double y1 = applyAsDouble(x1);
        double y2;
        for (int ii=1;ii<=points;ii++)
        {
            x1 += delta;
            y2 = applyAsDouble(x1);
            sum += Math.hypot(delta, (y2-y1));
            y1 = y2;
        }
        return sum;
    }
}
