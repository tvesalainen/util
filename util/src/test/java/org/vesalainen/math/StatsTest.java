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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StatsTest
{
    
    public StatsTest()
    {
    }

    @Test
    public void testMax()
    {
        assertEquals(123, Stats.max(-3, 100, -2, 4, 5, 6, 123, 87));
    }
    @Test
    public void testMin()
    {
        assertEquals(-3, Stats.min(-3, 100, -2, 4, 5, 6, 123, 87));
    }
    @Test
    public void testAvg()
    {
        assertEquals(2, Stats.avg(1, 2, 3, 4));
    }
    
}
