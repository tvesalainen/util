/*
 * Copyright (C) 2015 tkv
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

import org.vesalainen.navi.TrueWind;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class TrueWindTest
{
    private static final double Epsilon = 1e-8;
    public TrueWindTest()
    {
    }

    @Test
    public void test0()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(0);
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(0, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test01()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(45, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test02()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(75);
        tw.calc();
        assertEquals(75, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test03()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(195);
        tw.calc();
        assertEquals(195, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test04()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(285);
        tw.calc();
        assertEquals(285, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test05()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(0);
        tw.setRelativeSpeed(5);
        tw.setRelativeAngle(0);
        tw.calc();
        assertEquals(0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test1()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(Math.hypot(5, 5));
        tw.setRelativeAngle(45);
        tw.calc();
        assertEquals(90.0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test2()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(Math.hypot(5, 5));
        tw.setRelativeAngle(315);
        tw.calc();
        assertEquals(270.0, tw.getTrueAngle(), Epsilon);
        assertEquals(5, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test3()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(15);
        tw.setRelativeAngle(180);
        tw.calc();
        assertEquals(180.0, tw.getTrueAngle(), Epsilon);
        assertEquals(20, tw.getTrueSpeed(), Epsilon);
    }
    
    @Test
    public void test4()
    {
        TrueWind tw = new TrueWind();
        tw.setBoatSpeed(5);
        tw.setRelativeSpeed(14.3);
        tw.setRelativeAngle(180);
        tw.calc();
        assertEquals(180.0, tw.getTrueAngle(), Epsilon);
        assertEquals(19.3, tw.getTrueSpeed(), Epsilon);
    }
    
}
