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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MergerTest
{
    
    public MergerTest()
    {
    }

    @Test
    public void testComparator()
    {
        test(new IComparator());
    }
    @Test
    public void testNatural()
    {
        test(null);
    }
    private void test(Comparator<Integer> comp)
    {
        List<Integer> l1 = new ArrayList<>();
        l1.add(1);
        l1.add(1);
        l1.add(2);
        l1.add(3);
        l1.add(5);
        List<Integer> l2 = new ArrayList<>();
        l2.add(4);
        l2.add(6);
        l2.add(7);
        l2.add(8);
        Iterator<Integer> merged = Merger.merge(comp, l1.iterator(), l2.iterator());
        assertTrue(merged.hasNext());
        assertEquals(1, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(1, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(2, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(3, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(4, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(5, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(6, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(7, merged.next().intValue());
        assertTrue(merged.hasNext());
        assertEquals(8, merged.next().intValue());
        assertFalse(merged.hasNext());
    }
    private class IComparator implements Comparator<Integer>
    {

        @Override
        public int compare(Integer o1, Integer o2)
        {
            return o1.compareTo(o2);
        }
        
    }
}
