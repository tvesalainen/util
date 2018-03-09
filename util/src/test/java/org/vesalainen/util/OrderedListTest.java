/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class OrderedListTest
{
    
    public OrderedListTest()
    {
    }

    @Test
    public void test0()
    {
        OrderedList<Integer> ol = new OrderedList<>();
        ol.add(5);
        ol.add(2);
        ol.add(8);
        ol.add(1);
        ol.add(4);
        assertEquals(1, (int)ol.get(0));
        assertEquals(2, (int)ol.get(1));
        assertEquals(4, (int)ol.get(2));
        assertEquals(5, (int)ol.get(3));
        assertEquals(8, (int)ol.get(4));
    }
    @Test
    public void test1()
    {
        OrderedList<Integer> ol = new OrderedList<>();
        ol.add(1);
        ol.add(2);
        ol.add(3);
        ol.add(4);
        ol.add(4);
        ol.add(4);
        ol.add(4);
        ol.add(5);
        ol.add(6);
        ol.add(7);
        ol.add(10);
        ol.add(13);
        ol.add(15);
        assertEquals(1, (int)ol.tailIterator(-1, true).next());
        assertEquals(13, ol.tailStream(-1, true, false).count());
        assertEquals(4, (int)ol.tailIterator(4, true).next());
        assertEquals(10, ol.tailStream(4, true, false).count());
        assertEquals(5, (int)ol.tailIterator(4, false).next());
        assertEquals(10, (int)ol.tailIterator(9, false).next());
        assertFalse(ol.tailIterator(99, false).hasNext());
        assertEquals(3, ol.headStream(4, false, false).count());
        assertEquals(7, ol.headStream(4, true, false).count());
        assertEquals(10, ol.headStream(9, true, false).count());
        assertEquals(0, ol.indexOf(1));
        assertEquals(12, ol.indexOf(15));
        assertEquals(0, ol.lastIndexOf(1));
        assertEquals(12, ol.lastIndexOf(15));
        assertEquals(3, ol.indexOf(4));
        assertEquals(6, ol.lastIndexOf(4));
        assertEquals(-1, ol.indexOf(9));
        assertEquals(-1, ol.lastIndexOf(-1));
    }    
}
