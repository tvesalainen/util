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
public class HashBijectionTest
{
    
    public HashBijectionTest()
    {
    }

    @Test
    public void test1()
    {
        HashBijection<String,String> hb = new HashBijection<>();
        hb.put("f1", "x1");
        assertEquals("f1", hb.getFirst("x1"));
        assertEquals("x1", hb.getSecond("f1"));
        
        hb.put("f1", "x2");
        assertEquals("f1", hb.getFirst("x2"));
        assertEquals("x2", hb.getSecond("f1"));
        assertFalse(hb.containsFirst("x1"));
    }
    
    @Test
    public void test2()
    {
        HashBijection<String,String> hb = new HashBijection<>();
        hb.put("f1", "x1");
        assertEquals("f1", hb.getFirst("x1"));
        assertEquals("x1", hb.getSecond("f1"));
        
        hb.put("f2", "x1");
        assertEquals("f2", hb.getFirst("x1"));
        assertEquals("x1", hb.getSecond("f2"));
        assertFalse(hb.containsSecond("f1"));
    }
    
}
