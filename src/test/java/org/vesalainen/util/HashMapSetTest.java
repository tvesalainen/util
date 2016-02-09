/*
 * Copyright (C) 2015 tkv
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
public class HashMapSetTest
{
    
    public HashMapSetTest()
    {
    }

    @Test
    public void test1()
    {
        HashMapSet<String,String> hms = new HashMapSet<>();
        hms.add("a", "1");
        hms.add("a", "2");
        Set<String> lst = hms.get("a");
        assertEquals(2, lst.size());
        hms.removeItem("a", "2");
        lst = hms.get("a");
        assertEquals(1, lst.size());
        hms.removeItem("a", "1");
        assertNull(hms.get("a"));
    }
    
    
}
