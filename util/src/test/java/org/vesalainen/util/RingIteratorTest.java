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
package org.vesalainen.util;

import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingIteratorTest
{
    
    public RingIteratorTest()
    {
    }

    @Test
    public void test1()
    {
        TreeMap<Integer,String> tm = new TreeMap<Integer,String>();
        tm.put(1, "foo");
        tm.put(2, "bar");
        tm.put(3, "goo");
        tm.put(4, "boo");
        RingIterator<Integer,String> ri = new RingIterator(3, tm);
        assertTrue(ri.hasNext());
        assertEquals("goo", ri.next().getValue());
        assertTrue(ri.hasNext());
        assertEquals("boo", ri.next().getValue());
        assertTrue(ri.hasNext());
        assertEquals("foo", ri.next().getValue());
        assertTrue(ri.hasNext());
        assertEquals("bar", ri.next().getValue());
        assertFalse(ri.hasNext());
    }
    
}
