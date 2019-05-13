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
package org.vesalainen.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LongRingBufferMapTest
{
    
    public LongRingBufferMapTest()
    {
    }

    @Test
    public void test1()
    {
        LongRingBufferMap<String> m = new LongRingBufferMap<>(10);
        assertNull(m.getClosest(0, 0));
        assertTrue(m.isEmpty());
        m.put(1, "foo");
        assertFalse(m.isEmpty());
        m.put(2, "bar");
        m.put(3, "goo");
        assertEquals("bar", m.get(2));
        assertEquals("goo", m.get(3));
        assertEquals(3, m.keys().size());
        assertEquals(3, m.values().size());
        
        Set<Long> keys = new HashSet<>();
        List<String> values = new ArrayList<>();
        m.forEach((String s, long t)->
        {
            values.add(s);
            keys.add(t);
        });
        assertEquals(keys, m.keys());
        assertEquals(values, m.values());
    }
    @Test
    public void test2()
    {
        LongRingBufferMap<String> m = new LongRingBufferMap<>(2);
        m.put(10, "foo");
        m.put(20, "bar");
        m.put(30, "goo");
        assertEquals("bar", m.getClosest(16, 9));
        assertEquals("bar", m.getClosest(25, 9));
        assertNull(m.getClosest(2, 9));
    }
    
}
