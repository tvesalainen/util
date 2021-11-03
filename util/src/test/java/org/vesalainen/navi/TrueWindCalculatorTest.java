/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import static java.lang.Math.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrueWindCalculatorTest
{
    private static final double Epsilon = 1e-8;

    public TrueWindCalculatorTest()
    {
    }

    @Test
    public void test0()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(90);
        tw.setRelativeWindSpeed(5);
        tw.setTrueHeading(100);
        tw.setSpeedAngle(0);
        tw.setSpeed(0);
        assertEquals(5, tw.getTrueWindSpeed(), Epsilon);
        assertEquals(190, tw.getTrueWindAngle(), Epsilon);
    }
    @Test
    public void test1()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(90);
        tw.setRelativeWindSpeed(5);
        tw.setTrueHeading(0);
        tw.setSpeedAngle(90);
        tw.setSpeed(5);
        assertEquals(0, tw.getTrueWindSpeed(), Epsilon);
    }
    @Test
    public void test2()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(90);
        tw.setRelativeWindSpeed(5);
        tw.setTrueHeading(350);
        tw.setSpeedAngle(0);
        tw.setSpeed(0);
        assertEquals(5, tw.getTrueWindSpeed(), Epsilon);
        assertEquals(80, tw.getTrueWindAngle(), Epsilon);
    }
    @Test
    public void test3()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(0);
        tw.setRelativeWindSpeed(5);
        tw.setTrueHeading(270);
        tw.setSpeedAngle(270);
        tw.setSpeed(10);
        assertEquals(5, tw.getTrueWindSpeed(), Epsilon);
        assertEquals(90, tw.getTrueWindAngle(), Epsilon);
    }
    @Test
    public void test4()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(90);
        tw.setRelativeWindSpeed(5);
        tw.setTrueHeading(0);
        tw.setSpeedAngle(0);
        tw.setSpeed(5);
        assertEquals(Math.sqrt(5*5+5*5), tw.getTrueWindSpeed(), Epsilon);
        assertEquals(135, tw.getTrueWindAngle(), Epsilon);
    }
    @Test
    public void test5()
    {
        TrueWindCalculator tw = new TrueWindCalculator();
        tw.setRelativeWindAngle(90);
        tw.setRelativeWindSpeed(5);
        tw.setZeroAngle(10);
        tw.setTrueHeading(10);
        tw.setSpeedAngle(10);
        tw.setSpeed(5);
        assertEquals(Math.sqrt(5*5+5*5), tw.getTrueWindSpeed(), Epsilon);
        assertEquals(135, tw.getTrueWindAngle(), Epsilon);
    }
    // reusing TrueWind tests
    @Test
    public void testTW0()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(0);
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(0, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW01()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(45, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW02()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(75);
        tw.calc();
        assertEquals(75, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW03()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(195);
        tw.calc();
        assertEquals(195, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW04()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(285);
        tw.calc();
        assertEquals(285, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW05()
    {
        TW tw = new TW();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(0);
        tw.calc();
        assertEquals(0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW1()
    {
        TW tw = new TW();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(hypot(5, 5));
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(90.0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW2()
    {
        TW tw = new TW();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(hypot(5, 5));
        tw.setRelativeAngle(315);
        tw.calc();
        assertEquals(270.0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW3()
    {
        TW tw = new TW();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(15);
        tw.setRelativeAngle(180);
        tw.calc();
        assertEquals(180.0, tw.getTrueAngle(), Epsilon);
        assertEquals(20, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void testTW4()
    {
        TW tw = new TW();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(14.3);
        tw.setRelativeAngle(180);
        tw.calc();
        assertEquals(180.0, tw.getTrueAngle(), Epsilon);
        assertEquals(19.3, tw.getTrueSpeed(), Epsilon);
    }
    
    
    private class TW
    {
        private TrueWindCalculator calc = new TrueWindCalculator();
        
        private void setBoatSpeed(double speed)
        {
            calc.setSpeed(speed);
        }

        private void setRelativeSpeed(double speed)
        {
            calc.setRelativeWindSpeed(speed);
        }

        private void setRelativeAngle(double angle)
        {
            calc.setRelativeWindAngle(angle);
        }

        private void calc()
        {
        }

        private double getTrueSpeed()
        {
            return calc.getTrueWindSpeed();
        }

        private double getTrueAngle()
        {
            return calc.getTrueWindAngle();
        }
        
    }
}
