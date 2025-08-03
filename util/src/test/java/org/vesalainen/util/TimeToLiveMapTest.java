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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeToLiveMapTest
{
    
    public TimeToLiveMapTest()
    {
    }

    @Test
    public void test1()
    {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        TimeToLiveMap<Integer,String> map = new TimeToLiveMap<>(clock, 1, TimeUnit.SECONDS);
        map.put(1, "foo");
        map.put(2, "bar");
        map.put(3, "goo");
        assertEquals(3, map.size());
        assertEquals(3, map.values().stream().count());
        assertEquals(3, map.entrySet().stream().count());
        assertEquals(3, map.keySet().stream().count());
        assertEquals("bar", map.get(2));
        
        clock = Clock.offset(clock, Duration.ofSeconds(2));
        map.setClock(clock);
        assertEquals(0, map.size());
        assertEquals(0, map.entrySet().stream().count());
        assertEquals(0, map.keySet().stream().count());
        assertEquals(0, map.values().stream().count());
        
        map.put(2, "bar");
        map.put(3, "goo");
        assertEquals(2, map.size());
        assertEquals(2, map.values().stream().count());
        assertEquals("bar", map.get(2));
        
        map.remove(2);
        assertEquals(null, map.get(2));
        assertEquals(1, map.size());
        assertEquals(1, map.values().stream().count());
    }

}
