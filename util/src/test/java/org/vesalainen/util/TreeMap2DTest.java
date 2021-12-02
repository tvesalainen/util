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
package org.vesalainen.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TreeMap2DTest
{
    
    public TreeMap2DTest()
    {
    }

    @Test
    public void test1()
    {
        TreeMap2D<Integer,Integer,String> m = new TreeMap2D<>();
        m.put(1, 1, "(1,1)");
        m.put(1, 2, "(1,2)");
        m.put(3, 1, "(3,1)");
        assertEquals(3, m.size());
        assertEquals("(1,2)", m.get(1, 2));
        m.forEach((i, j, v)->System.err.println(i+", "+j+", "+v));
        m.remove(1, 2);
        assertEquals(2, m.size());
        assertNull(m.get(1, 2));
        m.clear();
        assertEquals(0, m.size());
    }
    
}
