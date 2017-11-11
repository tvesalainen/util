/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class BinaryMapTest
{
    BinaryMap<Integer,String> am;
    public BinaryMapTest()
    {
        am = new BinaryMap<>();
        am.put(1, "foo");
        am.put(3, "bar");
        am.put(5, "goo");
    }

    @Test
    public void testGet()
    {
        assertEquals("foo", am.get(1));
        assertEquals("bar", am.get(3));
        assertEquals("goo", am.get(5));
    }
    
    @Test
    public void testGet2()
    {
        assertEquals("foo", am.get(1, (k,v)->k==1).getValue());
        assertEquals("foo", am.get(0, (k,v)->k==1).getValue());
        assertEquals("foo", am.get(2, (k,v)->k==1).getValue());
        assertEquals("bar", am.get(2, (k,v)->k==3).getValue());
        assertEquals("bar", am.get(3, (k,v)->k==3).getValue());
        assertEquals("bar", am.get(4, (k,v)->k==3).getValue());
        assertEquals("goo", am.get(4, (k,v)->k==5).getValue());
        assertEquals("goo", am.get(5, (k,v)->k==5).getValue());
        assertEquals("goo", am.get(9, (k,v)->k==5).getValue());
    }
    
}
