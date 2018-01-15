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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BestFitLineTest
{
    
    public BestFitLineTest()
    {
    }

    @Test
    public void test1()
    {
        BestFitLine bfl = new BestFitLine();
        bfl.add(0, 1);
        bfl.add(1, 2);
        bfl.add(2, 3);
        double slope = bfl.getSlope();
        assertEquals(1, slope, 1e-10);
        assertEquals(1, bfl.getYIntercept(slope), 1e-10);
        assertEquals(1, bfl.getY(0), 1e-10);
        assertEquals(2, bfl.getY(1), 1e-10);
        assertEquals(3, bfl.getY(2), 1e-10);
    }
    
}
