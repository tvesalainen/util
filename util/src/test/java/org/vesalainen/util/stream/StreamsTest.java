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
package org.vesalainen.util.stream;

import java.util.List;
import org.junit.Test;
import org.vesalainen.util.CollectionHelp;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StreamsTest
{
    
    public StreamsTest()
    {
    }

    @Test
    public void test1()
    {
        List<Integer> l1 = CollectionHelp.create(4, 5, 2, 8, 5, 2);
        List<Integer> l2 = CollectionHelp.create(2, 4, 2, 8, 5, 5);
        assertTrue(Streams.equals(l1.stream().sorted(), l2.stream().sorted()));
    }
    
    @Test
    public void test2()
    {
        List<Integer> l1 = CollectionHelp.create(4, 5, 2, 8, 2);
        List<Integer> l2 = CollectionHelp.create(2, 4, 2, 8, 5, 5);
        assertFalse(Streams.equals(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test3()
    {
        List<Integer> l1 = CollectionHelp.create(4, 5, 2, 8, 5, 2);
        List<Integer> l2 = CollectionHelp.create(4, 5, 2, 7, 5, 2);
        assertFalse(Streams.equals(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test11()
    {
        List<Integer> l1 = CollectionHelp.create(4, 5, 2, 8, 5, 2);
        List<Integer> l2 = CollectionHelp.create(2, 4, 2, 8, 5, 5);
        assertEquals(0, Streams.compare(l1.stream().sorted(), l2.stream().sorted()));
    }
    
    @Test
    public void test12()
    {
        List<Integer> l1 = CollectionHelp.create(1, 2, 3, 4, 5, 6);
        List<Integer> l2 = CollectionHelp.create(1, 2, 3, 4, 5);
        assertEquals(1, Streams.compare(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test13()
    {
        List<Integer> l1 = CollectionHelp.create(1, 2, 3, 4, 5, 6);
        List<Integer> l2 = CollectionHelp.create(2, 2, 3, 4, 5);
        assertEquals(-1, Streams.compare(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test14()
    {
        List<Integer> l1 = CollectionHelp.create(1, 2, 3, 4, 6);
        List<Integer> l2 = CollectionHelp.create(1, 2, 3, 4, 5);
        assertEquals(1, Streams.compare(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test15()
    {
        List<Integer> l1 = CollectionHelp.create(1, 2, 3, 4, 6);
        List<Integer> l2 = CollectionHelp.create(1, 2, 3, 4, 5);
        assertEquals(1, Streams.compare(l1.stream(), l2.stream(), (Integer i1, Integer i2)->{return i1.compareTo(i2);}));
    }
    
}
