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
package org.vesalainen.ham.itshfbc;

import java.time.OffsetTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetTimeRangeTest
{
    
    public OffsetTimeRangeTest()
    {
    }

    @Test
    public void test1()
    {
        OffsetTimeRange r = new OffsetTimeRange("0900-1015");
        assertEquals("0900-1015", r.toString());
        OffsetTime in = OffsetTimeRange.parse("0915");
        OffsetTime out1 = OffsetTimeRange.parse("0859");
        OffsetTime out2 = OffsetTimeRange.parse("1016");
        assertTrue(r.isInside(in));
        assertFalse(r.isInside(out1));
        assertFalse(r.isInside(out2));
    }
    @Test
    public void test2()
    {
        OffsetTimeRange r = new OffsetTimeRange("1200-1015");
        assertEquals("1200-1015", r.toString());
        OffsetTime in1 = OffsetTimeRange.parse("1315");
        OffsetTime in2 = OffsetTimeRange.parse("0900");
        OffsetTime out1 = OffsetTimeRange.parse("1016");
        OffsetTime out2 = OffsetTimeRange.parse("1159");
        assertTrue(r.isInside(in1));
        assertTrue(r.isInside(in2));
        assertFalse(r.isInside(out1));
        assertFalse(r.isInside(out2));
    }
    
}
