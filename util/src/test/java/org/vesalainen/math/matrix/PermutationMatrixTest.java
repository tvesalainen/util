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
package org.vesalainen.math.matrix;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PermutationMatrixTest
{
    
    public PermutationMatrixTest()
    {
    }

    @Test
    public void test1()
    {
        PermutationMatrix pm = new PermutationMatrix(2);
        assertEquals(0, pm.get(0, 0));
        assertEquals(1, pm.get(0, 1));
        assertEquals(1, pm.get(1, 0));
        assertEquals(0, pm.get(1, 1));
    }
    
}
