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
package org.vesalainen.navi.dd1;

import static java.lang.Math.toDegrees;
import java.util.function.DoubleSupplier;
import org.vesalainen.math.SimpleLine;
import org.vesalainen.math.solver.RandomVariable;
import org.vesalainen.math.solver.Solver;
import org.vesalainen.math.solver.Variable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetAngleSolver extends DD1 implements Solver
{
    private SimpleLine line = new SimpleLine();
    
    public OffsetAngleSolver(double r2, double alpha, double beta, double a, double o)
    {
        super(r2, alpha, beta, a, o);
    }

    public TillerAngleSolver solve(double epsilon)
    {
        Variable r1 = new RandomVariable(20, 10);
        Variable oa = new RandomVariable(Math.toRadians(40), Math.toRadians(20));
        double[] solve = solveTimes(10000000, r1, oa);
        if (solve[0] > epsilon)
        {
            throw new IllegalArgumentException("not solved! Sum="+solve[0]);
        }
        return new TillerAngleSolver(solve[1], r2, toDegrees(alpha), toDegrees(beta), toDegrees(solve[2]), a, o);
    }
    @Override
    public double sum(DoubleSupplier... variables)
    {
        return sum(variables[0].getAsDouble(), variables[1].getAsDouble());
    }

    private double sum(double r1, double gamma)
    {
        double x10 = x10(r1, gamma);
        double y10 = y10(r1, gamma);
        double x20 = x20(r1, gamma);
        double y20 = y20(r1, gamma);
        line.set(x10, y10, x20, y20);
        double sum = 0;
        double x1L = x1(r1, gamma, -alpha);
        double y1L = y1(r1, gamma, -alpha);
        double x1R = x1(r1, gamma, alpha);
        double y1R = y1(r1, gamma, alpha);
        double x2L = x2(gamma, -beta);
        double y2L = y2(gamma, -beta);
        double x2R = x2(gamma, beta);
        double y2R = y2(gamma, beta);
        sum += sq(line.getY(x1L)-y1L);
        sum += sq(line.getY(x1R)-y1R);
        sum += sq(line.getY(x2L)-y2L);
        sum += sq(line.getY(x2R)-y2R);
        return sum;
    }
    
    private double sq(double x)
    {
        return x*x;
    }
}
