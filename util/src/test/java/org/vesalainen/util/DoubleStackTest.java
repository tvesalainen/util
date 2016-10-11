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
package org.vesalainen.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class DoubleStackTest
{

    private static final double Epsilon = 1e-10;
    
    public DoubleStackTest()
    {
    }

    @Test
    public void testGrow()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(1);
        s.push(2);
        s.push(3);
        s.push(4);
        s.push(5);
        assertEquals(5, s.size());
        assertEquals(5, s.pop(), Epsilon);
        assertEquals(4, s.pop(), Epsilon);
        assertEquals(3, s.pop(), Epsilon);
        assertEquals(2, s.pop(), Epsilon);
        assertEquals(1, s.pop(), Epsilon);
        assertTrue(s.isEmpty());
    }
    @Test
    public void testPush()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        assertTrue(s.isEmpty());
        s.push(Math.PI);
        assertEquals(1, s.size());
        assertEquals(Math.PI, s.peek(), Epsilon);
    }
    @Test
    public void testDup()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(Math.PI);
        s.dup();
        assertEquals(2, s.size());
        assertEquals(Math.PI, s.peek(), Epsilon);
    }
    @Test
    public void testAdd()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(Math.PI);
        s.dup();
        s.add();
        assertEquals(1, s.size());
        assertEquals(2*Math.PI, s.peek(), Epsilon);
    }
    @Test
    public void testDiv()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(Math.PI);
        s.push(2);
        s.div();
        assertEquals(1, s.size());
        assertEquals(Math.PI/2, s.peek(), Epsilon);
    }
    @Test
    public void testMod()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(7);
        s.push(2);
        s.mod();
        assertEquals(1, s.size());
        assertEquals(1, s.peek(), Epsilon);
    }
    @Test
    public void testMul()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(7);
        s.push(2);
        s.mul();
        assertEquals(1, s.size());
        assertEquals(14, s.peek(), Epsilon);
    }
    @Test
    public void testSubtract()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(7);
        s.push(2);
        s.subtract();
        assertEquals(1, s.size());
        assertEquals(5, s.peek(), Epsilon);
    }
    @Test
    public void testNeg()
    {
        DoubleStack s = new DoubleStack(4, 1.5F);
        s.push(7);
        s.neg();
        assertEquals(1, s.size());
        assertEquals(-7, s.peek(), Epsilon);
    }
    
}
