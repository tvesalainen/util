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
public class TimeSlidingAngleAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public TimeSlidingAngleAverageTest()
    {
    }

    @Test
    public void test1()
    {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        TimeoutSlidingAngleAverage tsaa = new TimeoutSlidingAngleAverage(clock, 2, 1000);
        tsaa.accept(10);
        assertEquals(10, tsaa.fast(), Epsilon);
        assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsaa.clock(clock);
        tsaa.accept(350);
        assertEquals(360, tsaa.fast(), Epsilon);
        assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsaa.clock(clock);
        tsaa.accept(30);
        assertEquals(10, tsaa.fast(), Epsilon);
        assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        tsaa.clock(clock);
        tsaa.accept(10);
        assertEquals(10, tsaa.fast(), Epsilon);
        assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
    }
    
    @Test
    public void test2()
    {
            TimeoutSlidingAngleAverage tsaa = new TimeoutSlidingAngleAverage(2, 1000);
            tsaa.accept(170);
            assertEquals(170, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            tsaa.accept(190);
            assertEquals(180, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
    }
    @Test
    public void test3()
    {
            TimeoutSlidingAngleAverage tsaa = new TimeoutSlidingAngleAverage(2, 1000);
            tsaa.accept(80);
            assertEquals(80, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            tsaa.accept(100);
            assertEquals(90, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
    }
}
