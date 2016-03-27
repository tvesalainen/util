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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class TimeoutSlidingStatsTest
{
    private static final double Epsilon = 1e-10;
    
    @Test
    public void testTimedSlidingMax()
    {
        try
        {
            // 10 9 8 7 6 9 10 11 9 8 7
            // --------
            //    -------
            //      -------
            //        --------
            //          ---------
            //            ---------
            //              ----------
            TimeoutSlidingStats sm = new TimeoutSlidingStats(3, 1000);
            sm.accept(10);
            long t1 = System.currentTimeMillis();
            assertEquals(10, sm.last(), Epsilon);
            assertEquals(t1, sm.lastTime(), 100L);
            Thread.sleep(300);
            assertEquals(10, sm.getMax(), Epsilon);
            sm.accept(9);
            long t2 = System.currentTimeMillis();
            assertEquals(10, sm.previous(), Epsilon);
            assertEquals(t1, sm.previousTime(), 100L);
            assertEquals(9, sm.last(), Epsilon);
            assertEquals(t2, sm.lastTime(), 100L);
            Thread.sleep(300);
            assertEquals(10, sm.getMax(), Epsilon);
            sm.accept(8);
            Thread.sleep(300);
            assertEquals(10, sm.getMax(), Epsilon);
            sm.accept(7);
            Thread.sleep(300);
            assertEquals(10, sm.getMax(), Epsilon);
            sm.accept(6);
            Thread.sleep(300);
            assertEquals(9, sm.getMax(), Epsilon);
            sm.accept(9);
            Thread.sleep(300);
            assertEquals(9, sm.getMax(), Epsilon);
            sm.accept(10);
            Thread.sleep(300);
            assertEquals(10, sm.getMax(), Epsilon);
            sm.accept(11);
            Thread.sleep(300);
            assertEquals(11, sm.getMax(), Epsilon);
            sm.accept(9);
            Thread.sleep(300);
            assertEquals(11, sm.getMax(), Epsilon);
            sm.accept(8);
            Thread.sleep(300);
            assertEquals(11, sm.getMax(), Epsilon);
            sm.accept(7);
            Thread.sleep(300);
            assertEquals(11, sm.getMax(), Epsilon);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SlidingBoundsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void testTimedSlidingMin()
    {
        try
        {
            // 10 9 8 7 6 9 10 11 9 8 7
            // -------- 1200
            //    ------- 1200-1500
            //      ------- 1500-1800
            //        -------- 1800-2100
            //          --------- 2100-2400
            //            --------- 2400-2700
            //              ---------- 2700-3000
            TimeoutSlidingStats sm = new TimeoutSlidingStats(3, 1000);
            sm.accept(10); // 0
            Thread.sleep(300);
            assertEquals(10, sm.getMin(), Epsilon);
            sm.accept(9);  // 300
            Thread.sleep(300);
            assertEquals(9, sm.getMin(), Epsilon);
            sm.accept(8);  // 600
            Thread.sleep(300);
            assertEquals(8, sm.getMin(), Epsilon);
            sm.accept(7);  // 900
            Thread.sleep(300);
            assertEquals(7, sm.getMin(), Epsilon);
            sm.accept(6);  // 1200
            Thread.sleep(300);
            assertEquals(6, sm.getMin(), Epsilon);
            sm.accept(9);  // 1500
            Thread.sleep(300);
            assertEquals(6, sm.getMin(), Epsilon);
            sm.accept(10); // 1800
            Thread.sleep(300);
            assertEquals(6, sm.getMin(), Epsilon);
            sm.accept(11); // 2100
            Thread.sleep(300);
            assertEquals(6, sm.getMin(), Epsilon);
            sm.accept(9);  // 2400
            Thread.sleep(300);
            assertEquals(9, sm.getMin(), Epsilon);
            sm.accept(8);  // 2700
            Thread.sleep(300);
            assertEquals(8, sm.getMin(), Epsilon);
            sm.accept(7);  // 3000
            Thread.sleep(300);
            assertEquals(7, sm.getMin(), Epsilon);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SlidingBoundsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    @Test
    public void testSomeMethod()
    {
    }
    
}
