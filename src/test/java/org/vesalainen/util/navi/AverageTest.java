/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.util.navi;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen
 */
public class AverageTest
{
    
    public AverageTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     */
    @Test
    public void test1()
    {
        System.out.println("average1");
        Random random = new Random(123456789L);
        Average average = new Average();
        int count = 10000;
        double epsilon = 0.001;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        double squareSum = 0;
        for (int ii=0;ii<count;ii++)
        {
            double v = random.nextGaussian();
            average.add(v);
            min = Math.min(min, v);
            max = Math.max(max, v);
            sum += v;
            squareSum += v*v;
        }
        assertEquals(sum/count, average.getAverage(), epsilon);
        assertEquals(Math.sqrt(squareSum/count), average.getDeviation(), epsilon);
        assertEquals(max, average.getMax(), epsilon);
        assertEquals(min, average.getMin(), epsilon);
        assertEquals(max-min, average.getRange(), epsilon);
    }

    @Test
    public void test2()
    {
        Average average = new Average(-180, 180, 10000, 123456789);
        assertEquals(0, average.getAverage(), 1);
        assertEquals(100, average.getDeviation(), 10);
        assertEquals(180, average.getMax(), 1);
        assertEquals(-180, average.getMin(), 1);
        assertEquals(360, average.getRange(), 1);
    }
}
