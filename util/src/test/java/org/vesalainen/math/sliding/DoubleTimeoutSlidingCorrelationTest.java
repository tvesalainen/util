/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import org.vesalainen.util.LongReference;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingCorrelationTest
{
    
    public DoubleTimeoutSlidingCorrelationTest()
    {
    }

    @Test
    public void test0()
    {
        LongReference time = new LongReference(0);
        DoubleTimeoutSlidingCorrelation c = new DoubleTimeoutSlidingCorrelation(()->time.getValue(), 64, 10, (l)->l/2.0);
        c.accept(0, 0);
        c.accept(1, 1);
        time.setValue(15);
        c.accept(2, 16);
        c.accept(3, 17);
        c.accept(4, 18);
        assertEquals(1, c.correlation(), 1e-10);
        assertEquals(c.correlation(), c.fast(), 1e-10);
    }
    
}
