/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class DoubleMapTest
{
    private static final double Epsilon = 1e-10;
    
    public DoubleMapTest()
    {
    }

    @Test
    public void test1()
    {
        DoubleMap<String> dm = new DoubleMap();
        dm.put("abc", 123.456);
        assertEquals(1, dm.size());
        assertEquals(123.456, dm.getDouble("abc"), Epsilon);
        dm.put("abc", 987.654);
        assertEquals(1, dm.size());
        assertEquals(987.654, dm.getDouble("abc"), Epsilon);
        dm.put("pi", Math.PI);
        assertEquals(2, dm.size());
        assertEquals(Math.PI, dm.getDouble("pi"), Epsilon);
        assertEquals(2, dm.keySet().size());
        assertEquals(2, dm.entrySet().size());
    }
    
}
