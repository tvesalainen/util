/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.sliding;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingSeriesTest
{
    
    public DoubleTimeoutSlidingSeriesTest()
    {
    }

    @Test
    public void test1()
    {
        DoubleTimeoutSlidingSeries s = new DoubleTimeoutSlidingSeries(16, Long.MAX_VALUE);
        s.accept(123, 0);
        s.accept(123, 10);
        assertEquals(1, s.count());
        assertEquals(10, s.lastTime());
    }
    
}
