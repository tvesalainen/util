/*
 * Copyright (C) 2015 tkv
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class RingSpanTest
{
    
    public RingSpanTest()
    {
    }

    @Test
    public void test1()
    {
        RingSpan s = new RingSpan(8);
        s.increment();
        assertEquals(0, s.start());
        assertEquals(1, s.end());
        assertEquals(1, s.length());
        
        s.increment(5);
        assertEquals(0, s.start());
        assertEquals(6, s.end());
        assertEquals(6, s.length());
        
        s.clear();
        s.increment(5);
        assertEquals(6, s.start());
        assertEquals(3, s.end());
        assertEquals(5, s.length());
        
    }
    
}