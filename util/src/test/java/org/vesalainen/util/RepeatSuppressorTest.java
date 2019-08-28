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
public class RepeatSuppressorTest
{
    private long millis;
    private int count;
    private long time;
    private String item;
    
    public RepeatSuppressorTest()
    {
    }

    @Test   // only one test!!!!!
    public void test()
    {
        RepeatSuppressor<String> rs = new RepeatSuppressor<String>(this::millis, this::forward, 5, 10, 100);
        
        rs.forward("foo");
        assertEquals(1, count);
        assertEquals(0, time);
        assertEquals("foo", item);
        
        millis = 1;

        rs.forward("foo");
        assertEquals(1, count);
        assertEquals(0, time);
        assertEquals("foo", item);
        
        millis = 2;

        rs.forward("bar");
        assertEquals(1, count);
        assertEquals(0, time);
        assertEquals("bar", item);
        
        millis = 6;

        rs.forward("foo");
        assertEquals(3, count);
        assertEquals(6, time);
        assertEquals("foo", item);
        
        millis = 111;

        rs.forward("foo");
        assertEquals(1, count);
        assertEquals(0, time);
        assertEquals("foo", item);
    }
    
    private long millis()
    {
        return millis;
    }
    private void forward(int count, long time, String item)
    {
        this.count = count;
        this.time = time;
        this.item = item;
    }
}
