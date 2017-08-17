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
package org.vesalainen.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MutableClockTest
{
    
    public MutableClockTest()
    {
    }

    @Test
    public void test1()
    {
        Clock fixed = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        MutableClock mc = new MutableClock(fixed);
        int year = 2001;
        int month = 3;
        int day = 12;
        int hour = 11;
        int minute = 33;
        int second = 54;
        int milliSecond = 123;
        mc.setDate(year, month, day);
        mc.setTime(hour, minute, second, milliSecond);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, milliSecond*1000000, ZoneOffset.UTC);
        assertEquals(zonedDateTime.toInstant().toEpochMilli(), mc.millis());
        assertEquals(year, mc.getYear());
        assertEquals(month, mc.getMonth());
        assertEquals(day, mc.getDay());
        assertEquals(hour, mc.getHour());
        assertEquals(minute, mc.getMinute());
        assertEquals(second, mc.getSecond());
        assertEquals(milliSecond, mc.getMilliSecond());
        mc.setClock(Clock.offset(fixed, Duration.ofMillis(500)));
        assertEquals(zonedDateTime.toInstant().toEpochMilli(), mc.millis() - 500);
    }
    
    @Test
    public void test2()
    {
        Clock fixed = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        ZonedDateTime now = ZonedDateTime.now(fixed);
        MutableClock mc = new MutableClock(fixed);
        mc.setMillis(now.toInstant().toEpochMilli());
        assertEquals(now, mc.getZonedDateTime());
        assertEquals(now.toInstant(), mc.instant());
    }
    
}
