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
package org.vesalainen.ham;

import org.vesalainen.ham.MonthDayRange;
import static java.time.Month.*;
import java.time.MonthDay;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MonthDayRangeTest
{
    
    public MonthDayRangeTest()
    {
    }

    @Test
    public void test1()
    {
        MonthDayRange r1 = new MonthDayRange(MonthDay.of(1, 31), MonthDay.of(5, 1));
        assertTrue(r1.isInside(MonthDay.of(FEBRUARY, 1)));
        assertTrue(r1.isInside(MonthDay.of(MAY, 1)));
        assertFalse(r1.isInside(MonthDay.of(JANUARY, 30)));
        
        MonthDayRange r2 = new MonthDayRange(r1.toDateRangeType());
        assertEquals(r1, r2);
    }
    @Test
    public void test2()
    {
        MonthDayRange r1 = new MonthDayRange(MonthDay.of(12, 1), MonthDay.of(1, 31));
        assertTrue(r1.isInside(MonthDay.of(JANUARY, 30)));
        assertFalse(r1.isInside(MonthDay.of(FEBRUARY, 1)));
        assertFalse(r1.isInside(MonthDay.of(MAY, 1)));

        MonthDayRange r2 = new MonthDayRange(r1.toDateRangeType());
        assertEquals(r1, r2);
    }
    
}
