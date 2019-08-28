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
package org.vesalainen.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeLimitIteratorTest
{
    
    public TimeLimitIteratorTest()
    {
    }

    @Test
    public void test1()
    {
        TimeLimitIterator tli = new TimeLimitIterator(()->0, 1, 10, 100);
        assertTrue(tli.hasNext());
        
        assertEquals(1L, tli.nextLong());
        assertEquals(10L, tli.nextLong());
        assertEquals(100L, tli.nextLong());
        assertEquals(200L, tli.nextLong());
        assertEquals(300L, tli.nextLong());
        assertEquals(400L, tli.nextLong());
    }
    
}
