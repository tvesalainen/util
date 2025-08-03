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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TillerAngleSolverTest
{
    
    public TillerAngleSolverTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        double r1 = 25;
        double oa = 0;
        double alpha = 36;
        double beta = 65;
        TillerAngleSolver tas = new TillerAngleSolver(r1, 16.5, alpha, beta, oa, 30, 13);
        assertEquals(alpha, tas.tillerAngle(beta), 1e-10);
        assertEquals(0, tas.tillerAngle(0), 1);
        assertEquals(-alpha, tas.tillerAngle(-beta), 2);
        tas.plot(Paths.get("center01.png"), Paths.get("angle01.png"));
    }
    
    @Test
    public void test2() throws IOException
    {
        double r1 = 25;
        double oa = 20;
        double alpha = 30;
        double beta = 65;
        TillerAngleSolver tas = new TillerAngleSolver(r1, 16.5, alpha, beta, oa, 30, 13);
        assertEquals(alpha, tas.tillerAngle(beta), 1e-10);
        assertEquals(0, tas.tillerAngle(0), 1);
        //assertEquals(-alpha, tas.tillerAngle(-beta), 2);
        tas.plot(Paths.get("center02.png"), Paths.get("angle02.png"));
    }
    
}
