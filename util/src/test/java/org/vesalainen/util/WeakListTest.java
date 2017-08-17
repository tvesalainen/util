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

import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WeakListTest
{
    
    public WeakListTest()
    {
    }

    //@Test result depends on environment
    public void testGC()
    {
        WeakList<byte[]> wl = new WeakList<>();
        for (int ii=0;ii<10;ii++)
        {
            wl.add(new byte[10000000]);
        }
        assertTrue(wl.size() < 10);
        assertTrue(wl.isGarbageCollected());
    }
    
    @Test
    public void testAdd()
    {
        WeakList<String> wl = new WeakList<>();
        wl.add("foo");
        wl.add("bar");
        wl.add("höö");
        assertEquals(3, wl.size());
        wl.remove("bar");
        assertEquals(2, wl.size());
        assertEquals("foohöö", wl.stream().collect(Collectors.joining()));
        assertFalse(wl.isGarbageCollected());
    }
    
    @Test
    public void testLoop()
    {
        WeakList<Integer> wl = new WeakList<>();
        wl.add(1);
        wl.add(1);
        wl.add(2);
        wl.add(3);
        wl.add(5);
        wl.add(8);
        wl.add(13);
        wl.add(21);
        int sum = 0;
        for (int ii : wl)
        {
            sum += ii;
        }
        assertEquals(sum, (int)wl.stream().collect(Collectors.summingInt(Integer::intValue)));
        assertFalse(wl.isGarbageCollected());
    }
}
