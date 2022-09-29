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
package org.vesalainen.math.sliding;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingBoundsTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingBoundsTest()
    {
    }

    @Test
    public void testSlidingMax()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        DoubleSlidingMax sm = new DoubleSlidingMax(3);
        sm.accept(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(8);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(7);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(6);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(11);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(8);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(7);
        assertEquals(9, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testSlidingMin()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        DoubleSlidingMin sm = new DoubleSlidingMin(3);
        sm.accept(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.accept(7);
        assertEquals(7, sm.getBound(), Epsilon);
        sm.accept(6);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(10);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(11);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.accept(7);
        assertEquals(7, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testTimedSlidingMax()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        // --------
        //    -------
        //      -------
        //        --------
        //          ---------
        //            ---------
        //              ----------
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        DoubleTimeoutSlidingMax sm = new DoubleTimeoutSlidingMax(clock, 3, 1000);
        sm.accept(10);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(9);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(8);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(7);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(6);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(9);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(10);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(11);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(9);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(8);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.accept(7);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testTimedSlidingMin()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        // -------- 1200
        //    ------- 1200-1500
        //      ------- 1500-1800
        //        -------- 1800-2100
        //          --------- 2100-2400
        //            --------- 2400-2700
        //              ---------- 2700-3000
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        DoubleTimeoutSlidingMin sm = new DoubleTimeoutSlidingMin(clock, 3, 1000);
        sm.accept(10); // 0
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.accept(9);  // 300
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(8);  // 600
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.accept(7);  // 900
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(7, sm.getBound(), Epsilon);
        sm.accept(6);  // 1200
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(9);  // 1500
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(10); // 1800
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(11); // 2100
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.accept(9);  // 2400
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.accept(8);  // 2700
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.accept(7);  // 3000
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(7, sm.getBound(), Epsilon);
    }
    
}
