/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeToLiveListTest
{
    
    public TimeToLiveListTest()
    {
    }

    @Test
    public void test0()
    {
        AtomicLong time = new AtomicLong();
        TimeToLiveList<String> ttll = new TimeToLiveList(()->time.get(), 2, TimeUnit.MILLISECONDS, (x)->{});
        ttll.add("a");
        assertEquals(1, ttll.size());
        time.incrementAndGet();
        ttll.add("b");
        assertEquals(2, ttll.size());
        time.incrementAndGet();
        ttll.add("c");
        assertEquals(2, ttll.size());
        time.incrementAndGet();
        ttll.add("d");
        assertEquals(2, ttll.size());
        time.incrementAndGet();
        ttll.add("e");
        assertEquals(2, ttll.size());
        assertEquals("d", ttll.get(0));
        assertEquals("e", ttll.get(1));
    }
    
}
