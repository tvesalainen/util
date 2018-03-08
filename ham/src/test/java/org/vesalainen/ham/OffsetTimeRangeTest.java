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

import org.vesalainen.ham.OffsetTimeRange;
import java.time.OffsetTime;
import java.time.ZoneOffset;
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
        OffsetTimeRange r1 = new OffsetTimeRange(9, 0, 10, 15);
        OffsetTime in = OffsetTime.of(9, 15, 0, 0, ZoneOffset.UTC);
        OffsetTime out1 = OffsetTime.of(8, 59, 0, 0, ZoneOffset.UTC);
        OffsetTime out2 = OffsetTime.of(10, 16, 0, 0, ZoneOffset.UTC);
        assertTrue(r1.isInRange(in));
        assertFalse(r1.isInRange(out1));
        assertFalse(r1.isInRange(out2));

        OffsetTimeRange r2 = new OffsetTimeRange(r1.toTimeRangeType());
        assertEquals(r1, r2);
    }
    @Test
    public void test2()
    {
        OffsetTimeRange r1 = new OffsetTimeRange(12, 0, 10, 15);
        OffsetTime in1 = OffsetTime.of(13, 15, 0, 0, ZoneOffset.UTC);
        OffsetTime in2 = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC);
        OffsetTime out1 = OffsetTime.of(10, 16, 0, 0, ZoneOffset.UTC);
        OffsetTime out2 = OffsetTime.of(11, 59, 0, 0, ZoneOffset.UTC);

        assertTrue(r1.isInRange(in1));
        assertTrue(r1.isInRange(in2));
        assertFalse(r1.isInRange(out1));
        assertFalse(r1.isInRange(out2));

        OffsetTimeRange r2 = new OffsetTimeRange(r1.toTimeRangeType());
        assertEquals(r1, r2);
    }
    
}
