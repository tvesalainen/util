/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi.dd1;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DD1Test
{
    
    public DD1Test()
    {
    }

    @Test
    public void test0()
    {
        double r1 = 25;
        double oa = 0;
        double a1 = 0;
        double a2 = 0;
        DD1 dd1 = new DD1(16.5, 36, 65, 30, 13);
        assertEquals(0, dd1.x1(r1, oa, a1), 1);
        assertEquals(r1, dd1.y1(r1, oa, a1), 1);
        assertEquals(30, dd1.x2(oa, a2), 1);
        assertEquals(29.5, dd1.y2(oa, a2), 1);
        
        assertEquals(0, dd1.x10(r1, oa), 1);
        assertEquals(21, dd1.y10(r1, oa), 1);
        assertEquals(30, dd1.x20(r1, oa), 1);
        assertEquals(20, dd1.y20(r1, oa), 1);
    }
    
    @Test
    public void testOffsetAngle()
    {
        double r1 = 25;
        double oa = Math.toRadians(10);
        double a1 = 0;
        double a2 = 0;
        DD1 dd1 = new DD1(16.5, 36, 65, 30, 13);
        
        assertEquals(4, dd1.x10(r1, oa), 1);
        assertEquals(19, dd1.y10(r1, oa), 1);
        assertEquals(30, dd1.x20(r1, oa), 1);
        assertEquals(19, dd1.y20(r1, oa), 1);
    }
    
    @Test
    public void testR()
    {
        double r1 = 25;
        double oa = 0;
        double a1 = Math.toRadians(36);
        double a2 = Math.toRadians(65);
        DD1 dd1 = new DD1(16.5, 36, 65, 30, 13);
        assertEquals(14, dd1.x1(r1, oa, a1), 1);
        assertEquals(21, dd1.y1(r1, oa, a1), 1);
        assertEquals(44, dd1.x2(oa, a2), 1);
        assertEquals(20, dd1.y2(oa, a2), 1);
        
    }
    
    @Test
    public void testL()
    {
        double r1 = 25;
        double oa = 0;
        double a1 = Math.toRadians(-36);
        double a2 = Math.toRadians(-65);
        DD1 dd1 = new DD1(16.5, 36, 65, 30, 13);
        assertEquals(-14, dd1.x1(r1, oa, a1), 1);
        assertEquals(21, dd1.y1(r1, oa, a1), 1);
        assertEquals(16, dd1.x2(oa, a2), 1);
        assertEquals(20, dd1.y2(oa, a2), 1);
        
    }
    
}
