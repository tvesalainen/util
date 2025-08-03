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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.util.RepeatingIteratorTest.En.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RepeatingIteratorTest
{
   enum En {E1, E2, E3};
   
    public RepeatingIteratorTest()
    {
    }

    @Test
    public void test1()
    {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        RepeatingIterator ri = new RepeatingIterator(list);
        assertTrue(ri.hasNext());
        assertEquals("foo", ri.next());
        assertTrue(ri.hasNext());
        assertEquals("bar", ri.next());
        assertTrue(ri.hasNext());
        assertEquals("foo", ri.next());
        assertTrue(ri.hasNext());
        assertEquals("bar", ri.next());
        assertTrue(ri.hasNext());
        assertEquals("foo", ri.next());
        assertTrue(ri.hasNext());
        assertEquals("bar", ri.next());
    }
    @Test
    public void test2()
    {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        RepeatingIterator ri = new RepeatingIterator(list, "foo");
        assertEquals("foo", ri.next());
    }
    @Test
    public void test3()
    {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        RepeatingIterator ri = new RepeatingIterator(list, "bar");
        assertEquals("bar", ri.next());
    }
    @Test
    public void test4()
    {
        Set<En> set = EnumSet.of(E1, E3);
        System.err.println(set);
        Set<En> set2 = new HashSet<>();
        set2.add(E1);
        set2.add(E3);
        System.err.println(set2);
        RepeatingIterator<En> ri = new RepeatingIterator<>(set, E3);
        ri.hasNext();
        assertEquals(E3, ri.next());
    }    
    @Test
    public void test4_b()
    {
        Set<En> set = EnumSet.allOf(En.class);
        Set<En> set2 = new ConditionalSet<>(set, (e)->e != E2);
        System.err.println(set2);
        RepeatingIterator<En> ri = new RepeatingIterator<>(set2, E3);
        ri.hasNext();
        assertEquals(E3, ri.next());
    }    
    @Test
    public void test5()
    {
        Set<En> set2 = new HashSet<>();
        set2.add(E1);
        set2.add(E3);
        System.err.println(set2);
        RepeatingIterator<En> ri = new RepeatingIterator<>(set2, E3);
        ri.hasNext();
        assertEquals(E3, ri.next());
    }    
}
