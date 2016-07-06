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
        SimpleMutableDate smt = new SimpleMutableDate();
        smt.setDate(1998, 11, 23);
        assertEquals(1998, smt.getYear());
        assertEquals(11, smt.getMonth());
        assertEquals(23, smt.getDay());
        smt.setTime(10, 11, 12, 13);
        assertEquals(10, smt.getHour());
        assertEquals(11, smt.getMinute());
        assertEquals(12, smt.getSecond());
        assertEquals(13, smt.getMilliSecond());

        SimpleMutableDate smt2 = new SimpleMutableDate();
        smt2.set(smt);
        assertTrue(smt.equals(smt2));
    }
    
    @Test
    public void test2()
    {
        ZonedDateTime zdt = ZonedDateTime.of(1959, 1, 31, 6, 30, 0, 0, ZoneOffset.UTC);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDate smt = new SimpleMutableDate();
        smt.setZonedDateTime(zdt);
        assertEquals(exp, smt.millis());
    }
    @Test
    public void test3()
    {
        ZoneOffset zo = ZoneOffset.ofHours(3);
        ZonedDateTime zdt = ZonedDateTime.of(1984, 4, 15, 13, 59, 12, 321000000, zo);
        long exp = zdt.toInstant().toEpochMilli();
        SimpleMutableDate smt = new SimpleMutableDate();
        smt.setZonedDateTime(zdt);
        assertEquals(exp, smt.millis(zo));
    }
}
