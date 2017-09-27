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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoUnit.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleMutableDateTimeTest
{
    
    public SimpleMutableDateTimeTest()
    {
    }

    @Test
    public void testSet()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime();
        smt.setDate(1998, 11, 23);
        assertEquals(1998, smt.getYear());
        assertEquals(11, smt.getMonth());
        assertEquals(23, smt.getDay());
        smt.setTime(10, 11, 12, 13);
        assertEquals(10, smt.getHour());
        assertEquals(11, smt.getMinute());
        assertEquals(12, smt.getSecond());
        assertEquals(13, smt.getMilliSecond());

        SimpleMutableDateTime smt2 = SimpleMutableDateTime.from(smt);
        assertTrue(smt.equals(smt2));
    }
    
    @Test
    public void testMillis1()
    {
        ZonedDateTime zdt = ZonedDateTime.of(1959, 1, 31, 6, 30, 0, 0, ZoneOffset.UTC);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDateTime smt = SimpleMutableDateTime.from(zdt);
        assertEquals(exp, smt.millis());
    }
    @Test
    public void testMillis2()
    {
        ZoneOffset zo = ZoneOffset.ofHours(3);
        ZonedDateTime zdt = ZonedDateTime.of(1984, 4, 15, 13, 59, 12, 321000000, zo);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDateTime smt = SimpleMutableDateTime.from(zdt);
        assertEquals(exp, smt.millis());
    }
    @Test
    public void testCompare()
    {
        SimpleMutableDateTime smt1 = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime smt2 = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 14);
        assertTrue(smt1.isBefore(smt2));
        assertTrue(smt2.isAfter(smt1));
    }
    @Test
    public void testZonedDateTime()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        ZonedDateTime zdt = ZonedDateTime.from(smt);
        for (TemporalField cf : smt.getFields().keySet())
        {
            assertEquals(smt.get(cf), zdt.get(cf));
        }
    }
    @Test
    public void testMilliSeconds()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = smt.clone();
        ZonedDateTime zdt = ZonedDateTime.from(smt);
        long millis = 10000000;
        smt.plusMilliSeconds(millis);
        ZonedDateTime pzdt = zdt.plusNanos(millis*1000000);
        for (TemporalField cf : smt.getFields().keySet())
        {
            assertEquals(smt.get(cf), pzdt.get(cf));
        }
        smt.plusMilliSeconds(-millis);
        assertEquals(exp, smt);
    }
    @Test
    public void testPlusDays()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1998, 11, 13, 10, 11, 12, 13);
        smt.plusDays(-10);
        assertEquals(exp, smt);
    }
    @Test
    public void testPlusMonths1()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1999, 9, 23, 10, 11, 12, 13);
        smt.plusMonths(10);
        assertEquals(exp, smt);
    }
    @Test
    public void testPlusMonths2()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1997, 3, 23, 10, 11, 12, 13);
        smt.plusMonths(-20);
        assertEquals(exp, smt);
    }
    @Test
    public void testPlusYears()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1997, 11, 23, 10, 11, 12, 13);
        smt.plusYears(-1);
        assertEquals(exp, smt);
    }
    @Test
    public void testPlus()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1998, 11, 13, 10, 11, 12, 13);
        Temporal plus = smt.plus(-10, DAYS);
        assertEquals(exp, plus);
    }
    @Test
    public void testPlusPlus()
    {
        SimpleMutableDateTime smt = SimpleMutableDateTime.epoch();
        ZonedDateTime zdt = ZonedDateTime.of(1998, 11, 23, 10, 11, 12, 13000000, ZoneOffset.of("Z"));
        SimpleMutableDateTime exp = SimpleMutableDateTime.from(zdt);
        Temporal plus = smt.plus(zdt.toEpochSecond()*1000+zdt.getNano()/1000000, MILLIS);
        assertEquals(exp, plus);
    }
    @Test
    public void testUntil()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13, ZoneId.of("EET"));
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1998, 11, 13, 10, 11, 12, 13, ZoneId.of("EET"));
        long until = smt.until(exp, DAYS);
        assertEquals(-10, until);
    }
    @Test
    public void testWith()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13, ZoneId.of("EET"));
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1998, 11, 13, 10, 11, 12, 13, ZoneId.of("EET"));
        Temporal with = smt.with(ChronoField.DAY_OF_MONTH, 13);
        assertEquals(exp, with);
    }
    @Test
    public void testFrom()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13, ZoneOffset.of("+0100"));
        ZonedDateTime exp = ZonedDateTime.of(1998, 11, 23, 10, 11, 12, 13000000, ZoneOffset.of("+0100"));
        ZonedDateTime from = ZonedDateTime.from(smt);
        assertEquals(exp, from);
    }
}
