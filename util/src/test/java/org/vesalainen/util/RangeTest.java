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

import java.time.Month;
import static java.time.Month.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeTest
{
    
    public RangeTest()
    {
    }

    @Test
    public void testIsInRange1()
    {
        Range<Month> r = new Range<>(FEBRUARY, JUNE);
        assertTrue(r.isInRange(FEBRUARY, true, true));
        assertTrue(r.isInRange(MAY, true, true));
        assertTrue(r.isInRange(JUNE, true, true));
        assertFalse(r.isInRange(JANUARY, true, true));
        assertFalse(r.isInRange(JULY, true, true));
    }
    @Test
    public void testIsInRange2()
    {
        Range<Month> r = new Range<>(JUNE, FEBRUARY);
        assertTrue(r.isInRange(FEBRUARY, true, true));
        assertFalse(r.isInRange(MAY, true, true));
        assertTrue(r.isInRange(JUNE, true, true));
        assertTrue(r.isInRange(JANUARY, true, true));
        assertTrue(r.isInRange(JULY, true, true));
    }
    @Test
    public void testIsInRange3()
    {
        Range<Month> r = new Range<>(FEBRUARY, JUNE);
        assertFalse(r.isInRange(FEBRUARY, false, false));
        assertTrue(r.isInRange(MAY, false, false));
        assertFalse(r.isInRange(JUNE, false, false));
        assertFalse(r.isInRange(JANUARY, false, false));
        assertFalse(r.isInRange(JULY, false, false));
    }
    @Test
    public void testIsOverlapping1()
    {
        Range<Month> r1 = new Range<>(FEBRUARY, JUNE);
        Range<Month> r2 = new Range<>(MARCH, DECEMBER);
        assertTrue(r1.isOverlapping(r1));
        assertTrue(r1.isOverlapping(r2));
        assertTrue(r2.isOverlapping(r1));
    }
    @Test
    public void testIsOverlapping2()
    {
        Range<Month> r1 = new Range<>(FEBRUARY, MARCH);
        Range<Month> r2 = new Range<>(MARCH, DECEMBER);
        assertTrue(r1.isOverlapping(r1));
        assertFalse(r1.isOverlapping(r2));
        assertFalse(r2.isOverlapping(r1));
    }
}
