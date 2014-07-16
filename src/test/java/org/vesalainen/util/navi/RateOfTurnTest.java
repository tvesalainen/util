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

import java.util.concurrent.TimeUnit;
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
        Distance expResult = new NauticalMile(1/Math.PI);
        Distance result = instance.getRadius(velocity);
        assertEquals(expResult, result);
    }

    /**
     * Test of isRight method, of class RateOfTurn.
     */
    @Test
    public void testIsRight()
    {
        System.out.println("isRight");
        RateOfTurn instance = new RateOfTurn(-10);
        boolean expResult = false;
        boolean result = instance.isRight();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBearingAfter method, of class RateOfTurn.
     */
    @Test
    public void testGetBearingAfter()
    {
        System.out.println("getBearingAfter");
        Angle bearing = new Degree(350);
        TimeSpan span = new TimeSpan(2, TimeUnit.MINUTES);
        RateOfTurn instance = new DegreesPerMinute(10);
        Angle expResult = new Degree(10);
        Angle result = instance.getBearingAfter(bearing, span);
        assertEquals(expResult, result);
    }

    /**
     * Test of getBearingAfter method, of class RateOfTurn.
     */
    @Test
    public void testGetBearingAfter2()
    {
        System.out.println("getBearingAfter");
        Angle bearing = new Degree(10);
        TimeSpan span = new TimeSpan(2, TimeUnit.MINUTES);
        RateOfTurn instance = new DegreesPerMinute(-10);
        Angle expResult = new Degree(350);
        Angle result = instance.getBearingAfter(bearing, span);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAngleChange method, of class RateOfTurn.
     */
    @Test
    public void testGetAngleChange()
    {
        System.out.println("getAngleChange");
        TimeSpan span = new TimeSpan(2, TimeUnit.MINUTES);
        RateOfTurn instance = new DegreesPerMinute(-10);
        Angle expResult = new Degree(340);
        Angle result = instance.getAngleChange(span);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMotionAfter method, of class RateOfTurn.
     */
    @Test
    public void testGetMotionAfter()
    {
        System.out.println("getMotionAfter");
        Motion motion = new Motion(new Knots(20), new Degree(350));
        TimeSpan span = new TimeSpan(2, TimeUnit.MINUTES);
        RateOfTurn instance = new DegreesPerMinute(10);
        Motion expResult = new Motion(new Knots(20), new Degree(10));
        Motion result = instance.getMotionAfter(motion, span);
        assertEquals(expResult, result);
    }

}
