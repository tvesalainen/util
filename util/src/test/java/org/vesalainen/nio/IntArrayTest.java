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
package org.vesalainen.nio;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IntArrayTest
{
    
    public IntArrayTest()
    {
    }

    @Test
    public void test1()
    {
        IntArray ia = IntArray.getInstance(new int[]{1, 2, 3, 4});
        assertEquals(10, ia.stream().sum());
    }
    @Test
    public void testCopy()
    {
        IntArray ia1 = IntArray.getInstance(new int[]{1, 2, 3, 4});
        IntArray ia2 = IntArray.getInstance(4);
        ia2.copy(ia1);
        assertEquals(10, ia2.stream().sum());
    }
    
}
