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
        SlidingMax sm = new SlidingMax(3);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(6);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(11);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(11, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(9, sm.getBound(), Epsilon);
    }
    
    @Test
    public void testSlidingMin()
    {
        // 10 9 8 7 6 9 10 11 9 8 7
        SlidingMin sm = new SlidingMin(3);
        sm.add(10);
        assertEquals(10, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
        sm.add(6);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(10);
        assertEquals(6, sm.getBound(), Epsilon);
        sm.add(11);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(9);
        assertEquals(9, sm.getBound(), Epsilon);
        sm.add(8);
        assertEquals(8, sm.getBound(), Epsilon);
        sm.add(7);
        assertEquals(7, sm.getBound(), Epsilon);
    }
    
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
            TimeoutSlidingMax sm = new TimeoutSlidingMax(3, 1000);
            sm.add(10);
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(9);
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(8);
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(7);
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(6);
            Thread.sleep(300);
            assertEquals(9, sm.getBound(), Epsilon);
            sm.add(9);
            Thread.sleep(300);
            assertEquals(9, sm.getBound(), Epsilon);
            sm.add(10);
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(11);
            Thread.sleep(300);
            assertEquals(11, sm.getBound(), Epsilon);
            sm.add(9);
            Thread.sleep(300);
            assertEquals(11, sm.getBound(), Epsilon);
            sm.add(8);
            Thread.sleep(300);
            assertEquals(11, sm.getBound(), Epsilon);
            sm.add(7);
            Thread.sleep(300);
            assertEquals(11, sm.getBound(), Epsilon);
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
            TimeoutSlidingMin sm = new TimeoutSlidingMin(3, 1000);
            sm.add(10); // 0
            Thread.sleep(300);
            assertEquals(10, sm.getBound(), Epsilon);
            sm.add(9);  // 300
            Thread.sleep(300);
            assertEquals(9, sm.getBound(), Epsilon);
            sm.add(8);  // 600
            Thread.sleep(300);
            assertEquals(8, sm.getBound(), Epsilon);
            sm.add(7);  // 900
            Thread.sleep(300);
            assertEquals(7, sm.getBound(), Epsilon);
            sm.add(6);  // 1200
            Thread.sleep(300);
            assertEquals(6, sm.getBound(), Epsilon);
            sm.add(9);  // 1500
            Thread.sleep(300);
            assertEquals(6, sm.getBound(), Epsilon);
            sm.add(10); // 1800
            Thread.sleep(300);
            assertEquals(6, sm.getBound(), Epsilon);
            sm.add(11); // 2100
            Thread.sleep(300);
            assertEquals(6, sm.getBound(), Epsilon);
            sm.add(9);  // 2400
            Thread.sleep(300);
            assertEquals(9, sm.getBound(), Epsilon);
            sm.add(8);  // 2700
            Thread.sleep(300);
            assertEquals(8, sm.getBound(), Epsilon);
            sm.add(7);  // 3000
            Thread.sleep(300);
            assertEquals(7, sm.getBound(), Epsilon);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SlidingBoundsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
