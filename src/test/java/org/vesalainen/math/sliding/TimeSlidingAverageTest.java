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
public class TimeSlidingAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public TimeSlidingAverageTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            TimeoutSlidingAverage tsa = new TimeoutSlidingAverage(2, 1000);
            tsa.accept(1);
            Thread.sleep(300);
            assertEquals(1, tsa.fast(), Epsilon);
            assertEquals(tsa.fast(), tsa.average(), Epsilon);
            tsa.accept(3);
            Thread.sleep(300);
            assertEquals(2, tsa.fast(), Epsilon);
            assertEquals(tsa.fast(), tsa.average(), Epsilon);
            tsa.accept(5);
            Thread.sleep(300);
            assertEquals(3, tsa.fast(), Epsilon);
            assertEquals(tsa.fast(), tsa.average(), Epsilon);
            Thread.sleep(300);
            tsa.accept(7);
            assertEquals(5, tsa.fast(), Epsilon);
            assertEquals(tsa.fast(), tsa.average(), Epsilon);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(TimeSlidingAverageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
