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

import java.util.function.DoubleSupplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface Solver
{
    double sum(DoubleSupplier... variables);
    default double[] solveTimes(int count, Variable... variables)
    {
        double[] res = new double[variables.length+1];
        double min = Double.MAX_VALUE;
        for (int ii=0;ii<count;ii++)
        {
            double sum = sum(variables);
            if (sum < min)
            {
                min = sum;
                res[0] = sum;
                for (int jj=0;jj<variables.length;jj++)
                {
                    res[jj+1] = variables[jj].getAsDouble();
                }
            }
            for (Variable var : variables)
            {
                var.update(sum);
            }
        }
        return res;
    }
    default double[] solve(double epsilon, Variable... variables)
    {
        double[] res = new double[variables.length+1];
        double min = Double.MAX_VALUE;
        double sum = Double.MAX_VALUE;
        while (sum > epsilon)
        {
            sum = sum(variables);
            if (sum < min)
            {
                min = sum;
                res[0] = sum;
                for (int jj=0;jj<variables.length;jj++)
                {
                    res[jj+1] = variables[jj].getAsDouble();
                }
            }
            for (Variable var : variables)
            {
                var.update(sum);
            }
        }
        return res;
    }
}
