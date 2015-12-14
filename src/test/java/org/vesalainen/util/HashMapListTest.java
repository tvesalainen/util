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

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class HashMapListTest
{
    
    public HashMapListTest()
    {
    }

    @Test
    public void test1()
    {
        HashMapList<String,String> hml = new HashMapList<>();
        hml.add("a", "1");
        hml.add("a", "2");
        List<String> lst = hml.get("a");
        assertEquals(2, lst.size());
        hml.removeItem("a", "2");
        lst = hml.get("a");
        assertEquals(1, lst.size());
        hml.removeItem("a", "1");
        lst = hml.get("a");
        assertEquals(0, lst.size());
    }
    
}
