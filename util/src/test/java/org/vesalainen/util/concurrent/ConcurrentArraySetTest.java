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
package org.vesalainen.util.concurrent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConcurrentArraySetTest
{
    
    public ConcurrentArraySetTest()
    {
    }

    @Test
    public void test1()
    {
        ConcurrentArraySet<String> cas = new ConcurrentArraySet<>();
        assertTrue(cas.add("foo"));
        assertTrue(cas.contains("foo"));
        assertFalse(cas.contains("bar"));
        assertFalse(cas.isEmpty());
        assertEquals(1, cas.size());
        assertFalse(cas.add("foo"));
        assertEquals(1, cas.size());
        assertTrue(cas.add("bar"));
        assertEquals(2, cas.size());
        assertTrue(cas.remove("foo"));
        assertEquals(1, cas.size());
        assertFalse(cas.contains("foo"));
        cas.clear();
        assertFalse(cas.contains("foo"));
        assertFalse(cas.contains("bar"));
        assertTrue(cas.isEmpty());
    }
    
    @Test
    public void test2()
    {
        List<Integer> f = new ArrayList<>();
        f.add(1);
        f.add(1);
        f.add(2);
        f.add(3);
        f.add(5);
        f.add(8);
        f.add(13);
        f.add(21);
        List<Integer> o = new ArrayList<>();
        o.add(1);
        o.add(3);
        o.add(5);
        o.add(7);
        o.add(9);
        o.add(11);
        o.add(13);
        ConcurrentArraySet<Integer> cas = new ConcurrentArraySet<>();
        assertTrue(cas.addAll(f));
        assertEquals(7, cas.size());
        assertTrue(cas.addAll(o));
        assertFalse(cas.addAll(o));
        assertEquals(10, cas.size());
        assertTrue(cas.containsAll(f));
        assertTrue(cas.containsAll(o));
        cas.retainAll(o);
        assertTrue(o.containsAll(cas));
        assertTrue(cas.containsAll(o));
        assertTrue(cas.removeAll(f));
        assertEquals(3, cas.size());
        assertTrue(cas.contains(7));
        assertTrue(cas.contains(9));
        assertTrue(cas.contains(11));
    }
    @Test
    public void test3()
    {
        List<Integer> o = new ArrayList<>();
        o.add(1);
        o.add(3);
        o.add(11);
        o.add(3);
        o.add(5);
        o.add(7);
        o.add(11);
        o.add(7);
        o.add(9);
        o.add(11);
        o.add(13);
        Set<Integer> exp = new HashSet<>();
        exp.addAll(o);
        Set<Integer> got = new HashSet<>();
        ConcurrentArraySet<Integer> cas = new ConcurrentArraySet<>();
        cas.addAll(o);
        for (Integer i : cas)
        {
            got.add(i);
        }
        assertEquals(exp, got);
        got.clear();
        for (Integer i : cas)
        {
            got.add(i);
        }
        assertEquals(exp, got);
    }
    @Test
    public void test4()
    {
        List<Integer> f = new ArrayList<>();
        f.add(1);
        f.add(1);
        f.add(2);
        f.add(3);
        f.add(5);
        f.add(8);
        f.add(13);
        f.add(21);
        List<Integer> o = new ArrayList<>();
        o.add(1);
        o.add(3);
        o.add(5);
        o.add(7);
        o.add(9);
        o.add(11);
        o.add(13);
        ConcurrentArraySet<Integer> cas = new ConcurrentArraySet<>();
        cas.addAll(f);
        cas.addAll(o);
        Set<Integer> exp = new HashSet<>();
        exp.addAll(f);
        exp.addAll(o);
        exp.removeAll(o);
        Iterator<Integer> iterator = cas.iterator();
        while (iterator.hasNext())
        {
            Integer i = iterator.next();
            if (o.contains(i))
            {
                iterator.remove();
            }
        }
        assertEquals(exp.size(), cas.size());
        assertTrue(cas.containsAll(exp));
        assertTrue(exp.containsAll(cas));
    }
    @Test
    public void test5()
    {
        ConcurrentArraySet<Integer> cas = new ConcurrentArraySet<>();
        cas.add(1);
        cas.add(3);
        cas.add(5);
        cas.add(7);
        cas.add(9);
        cas.add(11);
        cas.add(13);
        try
        {
            for (Integer i : cas)
            {
                if (i == 7)
                {
                    break;
                }
            }
        }
        finally
        {
            cas.unlock();
        }
    }
    @Test
    public void test6()
    {
        ConcurrentArraySet<Integer> cas = new ConcurrentArraySet<>();
        cas.add(1);
        cas.add(3);
        cas.add(5);
        cas.add(7);
        cas.add(9);
        cas.add(11);
        cas.add(13);
        try
        {
            for (Integer i : cas)
            {
                if (i == 0)
                {
                    break;
                }
            }
        }
        finally
        {
            cas.unlock();
        }
    }
}
