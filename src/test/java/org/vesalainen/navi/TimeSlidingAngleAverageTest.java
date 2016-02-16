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
package org.vesalainen.navi;

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
        try
        {
            TimeSlidingAngleAverage tsaa = new TimeSlidingAngleAverage(2, 1000);
            tsaa.add(10);
            assertEquals(10, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            Thread.sleep(300);
            tsaa.add(350);
            assertEquals(0, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            Thread.sleep(300);
            tsaa.add(30);
            assertEquals(10, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            Thread.sleep(300);
            tsaa.add(10);
            assertEquals(10, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(TimeSlidingAngleAverageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void test2()
    {
            TimeSlidingAngleAverage tsaa = new TimeSlidingAngleAverage(2, 1000);
            tsaa.add(170);
            assertEquals(170, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            tsaa.add(190);
            assertEquals(180, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
    }
    @Test
    public void test3()
    {
            TimeSlidingAngleAverage tsaa = new TimeSlidingAngleAverage(2, 1000);
            tsaa.add(80);
            assertEquals(80, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
            tsaa.add(100);
            assertEquals(90, tsaa.fast(), Epsilon);
            assertEquals(tsaa.average(), tsaa.fast(), Epsilon);
    }
}
