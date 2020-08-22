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

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.solver.LinearVariable;
import org.vesalainen.math.solver.RandomVariable;
import org.vesalainen.math.solver.Variable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetAngleSolverT
{
    
    public OffsetAngleSolverT()
    {
    }

    @Test
    public void test1() throws IOException
    {
        OffsetAngleSolver oas = new OffsetAngleSolver(16.5, 36, 65, 30, 13);
        TillerAngleSolver tas = oas.solve(300);
        tas.plot(Paths.get("center.png"), Paths.get("angle.png"));
    }
    
}
