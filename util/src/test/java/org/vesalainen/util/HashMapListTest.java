/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
    
    @Test
    public void test2()
    {
        HashMapList<String,String> hml = new HashMapList<>(new Reverse());
        hml.add("a", "1");
        hml.add("a", "2");
        List<String> lst = hml.get("a");
        assertEquals(2, lst.size());
        assertEquals("2", lst.get(0));
        assertEquals("1", lst.get(1));
    }
    
    private static class Reverse implements Comparator<String>
    {

        @Override
        public int compare(String o1, String o2)
        {
            return o2.compareTo(o1);
        }
        
    }
}
