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
package org.vesalainen.math.sliding;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class TimeSlidingAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public TimeSlidingAverageTest()
    {
    }

    @Test
    public void test1()
    {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        TimeoutSlidingAverage tsa = new TimeoutSlidingAverage(clock, 2, 1000);
        tsa.accept(1);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsa.clock(clock);
        assertEquals(1, tsa.fast(), Epsilon);
        assertEquals(tsa.fast(), tsa.average(), Epsilon);
        tsa.accept(3);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsa.clock(clock);
        assertEquals(2, tsa.fast(), Epsilon);
        assertEquals(tsa.fast(), tsa.average(), Epsilon);
        tsa.accept(5);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsa.clock(clock);
        assertEquals(3, tsa.fast(), Epsilon);
        assertEquals(tsa.fast(), tsa.average(), Epsilon);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsa.clock(clock);
        tsa.accept(7);
        assertEquals(5, tsa.fast(), Epsilon);
        assertEquals(tsa.fast(), tsa.average(), Epsilon);
    }
    
}
