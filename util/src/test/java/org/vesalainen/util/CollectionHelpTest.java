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
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CollectionHelpTest
{
    
    public CollectionHelpTest()
    {
    }

    @Test
    public void testSplit()
    {
        List<Integer> list = CollectionHelp.create(1, 2, -3, -4, 5, 6, -7);
        List<List<Integer>> split = CollectionHelp.split(list, (i)->i>=0);
        assertEquals(4, split.size());
        assertEquals(2, split.get(0).size());
        assertEquals(2, split.get(1).size());
        assertEquals(2, split.get(2).size());
        assertEquals(1, split.get(3).size());
    }
    @Test
    public void test1()
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, "a1", "a2", "a3");
        String exp = "{`a1´, `a2´, `a3´}";
        String got = CollectionHelp.print("{", ", ", "`", "´", "}", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test1_1()
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, "a1", "a2", "a3");
        String exp = "a1, a2, a3";
        String got = CollectionHelp.print(", ", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test2()
    {
        String exp = "{`a1´, `a2´, `a3´}";
        String got = CollectionHelp.print("{", ", ", "`", "´", "}", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
    @Test
    public void test2_1()
    {
        String exp = "a1, a2, a3";
        String got = CollectionHelp.print(", ", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
    @Test
    public void testFormat()
    {
        String exp = "1.00, 2.00, 3.00";
        String exp2 = "1.0, 2.0, 3.0";
        CollectionHelp.setFormat("%.2f", Locale.US);
        String got = CollectionHelp.print(", ", 1.0, 2.0, 3.0);
        assertEquals(exp, got);
        CollectionHelp.removeFormat();
        got = CollectionHelp.print(", ", 1.0, 2.0, 3.0);
        assertEquals(exp2, got);
    }
    
    @Test
    public void testEquals()
    {
        Integer[] arr = new Integer[] {1, 1, 2, 3, null, 8};
        List<Integer> list = CollectionHelp.create(arr);
        assertTrue(CollectionHelp.equals(list, arr));
        list.set(4, 5);
        assertFalse(CollectionHelp.equals(list, arr));
        list.add(9);
        assertFalse(CollectionHelp.equals(list, arr));
    }
    @Test
    public void testAddAll()
    {
        List<String> exp = CollectionHelp.create("a", "b", "1", "2", "3", "c", "d");
        List<String> list = CollectionHelp.create("a", "b", "c", "d");
        assertEquals(exp, CollectionHelp.addAll(2, list, "1", "2", "3"));
    }
    @Test
    public void testToArray()
    {
        String[] exp = new String[] {"foo", "bar", "goo"};
        List<String> list = CollectionHelp.create(exp);
        String[] array = CollectionHelp.toArray(list, String.class);
        assertArrayEquals(exp, array);
    }
    @Test
    public void testQuickSort()
    {
        Random random = new Random(12345678L);
        List<Integer> list = random.ints(100000).mapToObj(Integer::valueOf).collect(Collectors.toList());
        List<Integer> exp = new ArrayList<>(list);
        
        long s1 = nanos((x)->exp.sort(null));
        long s2 = nanos((x)->CollectionHelp.quickSort(list, null));
        System.err.println(s1+" sort "+s2+" quick");
        assertEquals(exp, list);
    }
    @Test
    public void testParallelQuickSort()
    {
        Random random = new Random(12345678L);
        List<Integer> list = random.ints(100000).mapToObj(Integer::valueOf).collect(Collectors.toList());
        List<Integer> exp = new ArrayList<>(list);
        
        long s1 = nanos((x)->exp.sort(null));
        long s2 = nanos((x)->CollectionHelp.parallelQuickSort(list, null));
        System.err.println("parallel "+s1+" sort "+s2+" quick");
        assertEquals(exp, list);
    }
    private long nanos(Consumer<?> action)
    {
        long time = System.nanoTime();
        action.accept(null);
        return System.nanoTime()-time;
    }
}
