/*
 * Copyright (C) 2016 tkv
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

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class LinkedMapTest
{
    
    public LinkedMapTest()
    {
    }

    @Test
    public void test1()
    {
        LinkedMap<String,String> lm = new LinkedMap<>();
        lm.put("c", "a");
        lm.put("b", "a");
        lm.put("a", "q");
        lm.put("a", "w");
        assertEquals(3, lm.size());
        assertEquals("c", lm.firstKey());
        assertEquals("a", lm.lastKey());
        lm.remove("b");
        assertEquals(2, lm.size());
        assertEquals(2, lm.keySet().size());
        assertEquals(2, lm.entrySet().size());
        assertEquals(2, lm.values().size());
    }
    
}
