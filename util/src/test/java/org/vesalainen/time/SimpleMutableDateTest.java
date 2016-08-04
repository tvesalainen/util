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
package org.vesalainen.time;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class SimpleMutableDateTest
{
    
    public SimpleMutableDateTest()
    {
    }

    @Test
    public void test1()
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

        SimpleMutableDateTime smt2 = new SimpleMutableDateTime();
        smt2.set(smt);
        assertTrue(smt.equals(smt2));
    }
    
    @Test
    public void test2()
    {
        ZonedDateTime zdt = ZonedDateTime.of(1959, 1, 31, 6, 30, 0, 0, ZoneOffset.UTC);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDateTime smt = new SimpleMutableDateTime();
        smt.setZonedDateTime(zdt);
        assertEquals(exp, smt.millis());
    }
    @Test
    public void test3()
    {
        ZoneOffset zo = ZoneOffset.ofHours(3);
        ZonedDateTime zdt = ZonedDateTime.of(1984, 4, 15, 13, 59, 12, 321000000, zo);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDateTime smt = new SimpleMutableDateTime();
        smt.setZonedDateTime(zdt);
        assertEquals(exp, smt.millis());
    }
    @Test
    public void test4()
    {
        SimpleMutableDateTime smt1 = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime smt2 = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 14);
        assertTrue(smt1.isBefore(smt2));
        assertTrue(smt2.isAfter(smt1));
    }
    @Test
    public void test5()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        ZonedDateTime zdt = smt.zonedDateTime();
        for (ChronoField cf : smt.getFields().keySet())
        {
            assertEquals(smt.get(cf), zdt.get(cf));
        }
    }
    @Test
    public void test6()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = smt.clone();
        ZonedDateTime zdt = smt.zonedDateTime();
        long millis = 10000000;
        smt.plusMilliSeconds(millis);
        ZonedDateTime pzdt = zdt.plusNanos(millis*1000000);
        for (ChronoField cf : smt.getFields().keySet())
        {
            assertEquals(smt.get(cf), pzdt.get(cf));
        }
        smt.plusMilliSeconds(-millis);
        assertEquals(exp, smt);
    }
    @Test
    public void test7()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1998, 11, 13, 10, 11, 12, 13);
        smt.plusDays(-10);
        assertEquals(exp, smt);
    }
    @Test
    public void test8()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1999, 9, 23, 10, 11, 12, 13);
        smt.plusMonths(10);
        assertEquals(exp, smt);
    }
    @Test
    public void test9()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1997, 3, 23, 10, 11, 12, 13);
        smt.plusMonths(-20);
        assertEquals(exp, smt);
    }
    @Test
    public void test10()
    {
        SimpleMutableDateTime smt = new SimpleMutableDateTime(1998, 11, 23, 10, 11, 12, 13);
        SimpleMutableDateTime exp = new SimpleMutableDateTime(1997, 11, 23, 10, 11, 12, 13);
        smt.plusYears(-1);
        assertEquals(exp, smt);
    }
}