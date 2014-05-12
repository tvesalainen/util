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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class RateOfTurnTest
{
    
    public RateOfTurnTest()
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
     * Test of getDegreesPerMinute method, of class RateOfTurn.
     */
    @Test
    public void testGetDegreesPerMinute()
    {
        System.out.println("getDegreesPerMinute");
        RateOfTurn instance = new DegreesPerMinute(1.5);
        double expResult = 1.5;
        double result = instance.getDegreesPerMinute();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getSecondsForFullCircle method, of class RateOfTurn.
     */
    @Test
    public void testGetSecondsForFullCircle()
    {
        System.out.println("getSecondsForFullCircle");
        RateOfTurn instance = new DegreesPerMinute(180);
        double expResult = 120;
        double result = instance.getSecondsForFullCircle();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getTimeForFullCircle method, of class RateOfTurn.
     */
    @Test
    public void testGetTimeForFullCircle()
    {
        System.out.println("getTimeForFullCircle");
        RateOfTurn instance = new DegreesPerMinute(180);
        TimeSpan expResult = new TimeSpan(120000);
        TimeSpan result = instance.getTimeForFullCircle();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRadius method, of class RateOfTurn.
     */
    @Test
    public void testGetRadius()
    {
        System.out.println("getRadius");
        Velocity velocity = new Knots(1);
        RateOfTurn instance = new DegreesPerMinute(6);
        Distance expResult = new Miles(1/Math.PI);
        Distance result = instance.getRadius(velocity);
        assertEquals(expResult, result);
    }

}
