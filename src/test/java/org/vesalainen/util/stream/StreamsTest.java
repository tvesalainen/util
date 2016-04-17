/*
 * Copyright (C) 2016 tkv
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
import org.vesalainen.util.Lists;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class StreamsTest
{
    
    public StreamsTest()
    {
    }

    @Test
    public void test1()
    {
        List<Integer> l1 = Lists.create(4, 5, 2, 8, 5, 2);
        List<Integer> l2 = Lists.create(2, 4, 2, 8, 5, 5);
        assertTrue(Streams.equals(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test2()
    {
        List<Integer> l1 = Lists.create(4, 5, 2, 8, 2);
        List<Integer> l2 = Lists.create(2, 4, 2, 8, 5, 5);
        assertFalse(Streams.equals(l1.stream(), l2.stream()));
    }
    
    @Test
    public void test3()
    {
        List<Integer> l1 = Lists.create(4, 5, 2, 8, 5, 2);
        List<Integer> l2 = Lists.create(2, 9, 2, 8, 5, 5);
        assertFalse(Streams.equals(l1.stream(), l2.stream()));
    }
    
}
