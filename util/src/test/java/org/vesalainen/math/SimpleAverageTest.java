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
public class SimpleAverageTest
{
    
    public SimpleAverageTest()
    {
    }

    @Test
    public void test1()
    {
        SimpleAverage sa = new SimpleAverage();
        sa.add(1, 2);
        sa.add(4, 1);
        assertEquals(2.0, sa.average(), 1e-10);
    }
    @Test
    public void test2()
    {
        SimpleAverage sa = new SimpleAverage();
        sa.add(1);
        sa.add(2);
        sa.add(3);
        assertEquals(2.0, sa.average(), 1e-10);
    }
    @Test
    public void test3()
    {
        SimpleAverage sa = new SimpleAverage();
        sa.add(1, 2, 3);
        assertEquals(2.0, sa.average(), 1e-10);
    }
}
