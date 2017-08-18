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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ListsTest
{
    
    public ListsTest()
    {
    }

    @Test
    public void test1()
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, "a1", "a2", "a3");
        String exp = "{`a1´, `a2´, `a3´}";
        String got = Lists.print("{", ", ", "`", "´", "}", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test1_1()
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, "a1", "a2", "a3");
        String exp = "a1, a2, a3";
        String got = Lists.print(", ", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test2()
    {
        String exp = "{`a1´, `a2´, `a3´}";
        String got = Lists.print("{", ", ", "`", "´", "}", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
    @Test
    public void test2_1()
    {
        String exp = "a1, a2, a3";
        String got = Lists.print(", ", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
    @Test
    public void testFormat()
    {
        String exp = "1.00, 2.00, 3.00";
        String exp2 = "1.0, 2.0, 3.0";
        Lists.setFormat("%.2f", Locale.US);
        String got = Lists.print(", ", 1.0, 2.0, 3.0);
        assertEquals(exp, got);
        Lists.removeFormat();
        got = Lists.print(", ", 1.0, 2.0, 3.0);
        assertEquals(exp2, got);
    }
    
    @Test
    public void testEquals()
    {
        Integer[] arr = new Integer[] {1, 1, 2, 3, null, 8};
        List<Integer> list = Lists.create(arr);
        assertTrue(Lists.equals(list, arr));
        list.set(4, 5);
        assertFalse(Lists.equals(list, arr));
        list.add(9);
        assertFalse(Lists.equals(list, arr));
    }
    @Test
    public void testAddAll()
    {
        List<String> exp = Lists.create("a", "b", "1", "2", "3", "c", "d");
        List<String> list = Lists.create("a", "b", "c", "d");
        assertEquals(exp, Lists.addAll(2, list, "1", "2", "3"));
    }
}
