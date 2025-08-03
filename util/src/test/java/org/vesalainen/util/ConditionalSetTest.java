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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConditionalSetTest
{
    
    public ConditionalSetTest()
    {
    }

    @Test
    public void test1()
    {
        ConditionalSet<Integer> cs = new ConditionalSet<>((i)->i%2==0);
        
        cs.add(1);
        assertTrue(cs.isEmpty());
        assertFalse(cs.contains(1));
        assertFalse(cs.iterator().hasNext());
        assertEquals(0, cs.size());
        
        cs.add(2);
        assertFalse(cs.isEmpty());
        assertTrue(cs.contains(2));
        Iterator<Integer> iterator = cs.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(2, (int)iterator.next());
        assertEquals(1, cs.size());
    }
    
}
