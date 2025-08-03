/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class BoundedPriorityQueueTest
{
    
    public BoundedPriorityQueueTest()
    {
    }

    @Test
    public void test1()
    {
        BoundedPriorityQueue<Integer> q = new BoundedPriorityQueue<Integer>(3);
        q.offer(9);
        assertEquals(1, q.size());
        assertEquals((Integer)9, q.peek());
        q.offer(9);
        assertEquals(2, q.size());
        assertEquals((Integer)9, q.peek());
        q.offer(7);
        assertEquals(3, q.size());
        assertEquals((Integer)7, q.peek());
        q.offer(6);
        assertEquals(3, q.size());
        assertEquals((Integer)6, q.peek());
        
        assertEquals((Integer)6, q.poll());
        assertEquals(2, q.size());
        assertEquals((Integer)7, q.poll());
        assertEquals(1, q.size());
        assertEquals((Integer)9, q.poll());
        assertEquals(0, q.size());
    }
    
}
