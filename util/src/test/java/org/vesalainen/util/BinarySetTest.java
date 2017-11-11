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

import java.util.Iterator;
import java.util.NavigableSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BinarySetTest
{
    BinarySet<Integer> as;
    public BinarySetTest()
    {
        as = create(1, 3, 5, 7, 7, 9, 11, 13, 13, 15, 17);
    }

    private BinarySet<Integer> create(Integer... is)
    {
        return new BinarySet<>(Lists.create(is));
    }
    @Test
    public void testContains()
    {
        assertTrue(as.contains(5));
        assertFalse(as.contains(4));
    }
    @Test
    public void testSize()
    {
        assertEquals(9, as.size());
    }
    @Test
    public void testSubSet()
    {
        assertEquals(2, as.subSet(3, 7).size());
        assertEquals(2, as.subSet(3, true, 7, false).size());
        assertEquals(2, as.subSet(3, 6).size());
        assertEquals(2, as.subSet(2, 6).size());
        assertEquals(1, as.subSet(3, false, 7, false).size());
        assertEquals(2, as.subSet(3, false, 7, true).size());
    }
    @Test
    public void testHeadSet()
    {
        assertEquals(3, as.headSet(7).size());
        assertEquals(4, as.headSet(7, true).size());
    }
    @Test
    public void testTailSet()
    {
        assertEquals(6, as.tailSet(7).size());
        assertEquals(5, as.tailSet(7, false).size());
    }
    @Test
    public void testFirst()
    {
        assertEquals(1, (int)as.first());
    }
    @Test
    public void testLast()
    {
        assertEquals(17, (int)as.last());
    }
    @Test
    public void testLower()
    {
        assertEquals(13, (int)as.lower(14));
        assertEquals(13, (int)as.lower(15));
        assertNull(as.lower(1));
        assertNull(as.lower(0));
    }
    @Test
    public void testHigher()
    {
        assertEquals(13, (int)as.higher(11));
        assertEquals(13, (int)as.higher(12));
        assertNull(as.higher(17));
        assertNull(as.higher(18));
    }
    @Test
    public void testFloor()
    {
        assertEquals(13, (int)as.floor(13));
        assertEquals(13, (int)as.floor(14));
        assertNull(as.floor(0));
    }
    @Test
    public void testCeiling()
    {
        assertEquals(13, (int)as.ceiling(13));
        assertEquals(13, (int)as.ceiling(12));
        assertNull(as.ceiling(18));
    }
    @Test
    public void testPollFirst()
    {
        NavigableSet<Integer> subSet = (NavigableSet<Integer>) as.subSet(3, 7);
        assertEquals(3, (int)subSet.pollFirst());
        assertEquals(5, (int)subSet.pollFirst());
        assertNull(subSet.pollFirst());
    }
    @Test
    public void testPollLast()
    {
        NavigableSet<Integer> subSet = (NavigableSet<Integer>) as.subSet(3, 7);
        assertEquals(5, (int)subSet.pollLast());
        assertEquals(3, (int)subSet.pollLast());
        assertNull(subSet.pollLast());
    }
    @Test
    public void testIterator()
    {
        Iterator<Integer> iterator = as.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(1, (int)iterator.next());
        assertEquals(3, (int)iterator.next());
        assertEquals(5, (int)iterator.next());
        assertEquals(7, (int)iterator.next());
        assertEquals(9, (int)iterator.next());
        assertEquals(11, (int)iterator.next());
        assertEquals(13, (int)iterator.next());
        assertEquals(15, (int)iterator.next());
        assertEquals(17, (int)iterator.next());
        assertFalse(iterator.hasNext());
    }
    @Test
    public void testDescendingIterator()
    {
        Iterator<Integer> iterator = as.descendingIterator();
        assertTrue(iterator.hasNext());
        assertEquals(17, (int)iterator.next());
        assertEquals(15, (int)iterator.next());
        assertEquals(13, (int)iterator.next());
        assertEquals(11, (int)iterator.next());
        assertEquals(9, (int)iterator.next());
        assertEquals(7, (int)iterator.next());
        assertEquals(5, (int)iterator.next());
        assertEquals(3, (int)iterator.next());
        assertEquals(1, (int)iterator.next());
        assertFalse(iterator.hasNext());
    }
    @Test
    public void testDescendingSet()
    {
        NavigableSet<Integer> descendingSet = as.descendingSet();
        Iterator<Integer> iterator = descendingSet.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(17, (int)iterator.next());
        assertEquals(15, (int)iterator.next());
        assertEquals(13, (int)iterator.next());
        assertEquals(11, (int)iterator.next());
        assertEquals(9, (int)iterator.next());
        assertEquals(7, (int)iterator.next());
        assertEquals(5, (int)iterator.next());
        assertEquals(3, (int)iterator.next());
        assertEquals(1, (int)iterator.next());
        assertFalse(iterator.hasNext());
        assertEquals(as, descendingSet.descendingSet());
    }
    @Test
    public void testSearch()
    {
        assertEquals(13, (int)as.get(13, (i)->i>=13));
        assertEquals(13, (int)as.get(12, (i)->i>=13));
        assertEquals(13, (int)as.get(14, (i)->i>=13));
        assertNull(as.get(11, (i)->i>=13));
        assertNull(as.get(0, (i)->i>=13));
        assertEquals(17, (int)as.get(99, (i)->i>=13));
    }
}
