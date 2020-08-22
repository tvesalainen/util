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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolverTest
{
    
    public SolverTest()
    {
    }

    @Test
    public void testLinear()
    {
        ParaabelSolver ps = new ParaabelSolver();
        LinearVariable lv = new LinearVariable(10, 15);
        double[] solved = ps.solve(0, lv);
        assertEquals(0, solved[0], 1e-10);
    }
    
    @Test
    public void testRandom()
    {
        ParaabelSolver ps = new ParaabelSolver();
        RandomVariable rv = new RandomVariable(10, 15);
        double[] solved = ps.solve(0.001, rv);
        assertEquals(0, solved[0], 0.1);
    }
    
    public class ParaabelSolver implements Solver
    {

        @Override
        public double sum(DoubleSupplier... variables)
        {
            double x = variables[0].getAsDouble();
            return x*x;
        }
        
    }
}
