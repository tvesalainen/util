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
public class TimeoutSlidingStatsTest
{
    private static final double Epsilon = 1e-10;
    
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
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        TimeoutSlidingStats sm = new TimeoutSlidingStats(clock, 3, 1000);
        sm.accept(10);  // 0
        long t1 = clock.millis();
        assertEquals(10, sm.last(), Epsilon);
        assertEquals(t1, sm.lastTime(), 100L);
        assertEquals(t1, sm.firstTime(), 100L);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getMax(), Epsilon);
        sm.accept(9);   // 300
        long t2 = clock.millis();
        assertEquals(10, sm.previous(), Epsilon);
        assertEquals(t1, sm.previousTime(), 100L);
        assertEquals(9, sm.last(), Epsilon);
        assertEquals(t2, sm.lastTime(), 100L);
        assertEquals(t1, sm.firstTime(), 100L);
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getMax(), Epsilon);
        sm.accept(8);   // 600
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getMax(), Epsilon);
        sm.accept(7);   // 900
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getMax(), Epsilon);
        sm.accept(6);   // 1200
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getMax(), Epsilon);
        sm.accept(9);   // 1500
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(9, sm.getMax(), Epsilon);
        sm.accept(10);  // 1800
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(10, sm.getMax(), Epsilon);
        sm.accept(11);  // 2100
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getMax(), Epsilon);
        sm.accept(9);   // 2400
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getMax(), Epsilon);
        sm.accept(8);   // 2700
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getMax(), Epsilon);
        sm.accept(7);   // 3000
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        assertEquals(11, sm.getMax(), Epsilon);
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
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        TimeoutSlidingStats sm = new TimeoutSlidingStats(clock, 3, 1000);
        sm.accept(10); // 0
        assertEquals(10, sm.getMin(), Epsilon);

        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(9);  // 300
        assertEquals(9, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(8);  // 600
        assertEquals(8, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(7);  // 900
        assertEquals(7, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(6);  // 1200
        assertEquals(6, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(9);  // 1500
        assertEquals(6, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(10); // 1800
        assertEquals(6, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(11); // 2100
        assertEquals(6, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(9);  // 2400
        assertEquals(9, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(8);  // 2700
        assertEquals(8, sm.getMin(), Epsilon);
        
        clock = Clock.offset(clock, Duration.ofMillis(300));
        sm.clock(clock);
        sm.accept(7);  // 3000
        assertEquals(7, sm.getMin(), Epsilon);
    }
    

}
