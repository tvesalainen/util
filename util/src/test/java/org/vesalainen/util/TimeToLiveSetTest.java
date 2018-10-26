/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeToLiveSetTest
{
    
    public TimeToLiveSetTest()
    {
    }

    @Test
    public void test1()
    {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        TimeToLiveSet<String> ttls = new TimeToLiveSet<>(clock, 1, TimeUnit.SECONDS);
        ttls.refresh("abc");
        assertEquals(1, ttls.size());
        assertTrue(ttls.isAlive("abc"));
        assertFalse(ttls.isAlive("zxc"));
        ttls.refresh("zxc", 3, TimeUnit.SECONDS);
        assertEquals(2, ttls.size());
        assertTrue(ttls.isAlive("zxc"));
        Set<String> set = ttls.set();
        assertEquals(2, set.size());
        assertTrue(set.contains("abc"));
        assertTrue(set.contains("zxc"));
        clock = Clock.offset(clock, Duration.ofSeconds(2));
        ttls.setClock(clock);
        assertFalse(ttls.isAlive("abc"));
        assertEquals(1, ttls.size());
        clock = Clock.offset(clock, Duration.ofSeconds(2));
        ttls.setClock(clock);
        assertFalse(ttls.isAlive("zxc"));
        assertEquals(0, ttls.size());
    }
    
}
