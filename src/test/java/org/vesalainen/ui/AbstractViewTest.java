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

package org.vesalainen.ui;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractViewTest
{
    private static final double Epsilon = 1e-10;
    
    public AbstractViewTest()
    {
    }

    @Test
    public void testView1()
    {
        AbstractView view = new AbstractView(-10, 10, -10, 10);
        view.setScreen(100, 200);
        assertTrue(view.isReady());
        assertEquals(0, view.toScreenX(-10), Epsilon);
        assertEquals(100, view.toScreenX(10), Epsilon);
        assertEquals(150, view.toScreenY(-10), Epsilon);
        assertEquals(50, view.toScreenY(10), Epsilon);
        assertEquals(50, view.toScreenX(0), Epsilon);
        assertEquals(100, view.toScreenY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
        
        assertEquals(-10, view.fromScreenX(0), Epsilon);
        assertEquals(10, view.fromScreenX(100), Epsilon);
        assertEquals(-10, view.fromScreenY(150), Epsilon);
        assertEquals(10, view.fromScreenY(50), Epsilon);
        assertEquals(0, view.fromScreenX(50), Epsilon);
        assertEquals(0, view.fromScreenY(100), Epsilon);
    }
    
    @Test
    public void testView2()
    {
        AbstractView view = new AbstractView(-10, 10, -10, 10);
        view.setScreen(200, 100);
        assertTrue(view.isReady());
        assertEquals(50, view.toScreenX(-10), Epsilon);
        assertEquals(150, view.toScreenX(10), Epsilon);
        assertEquals(100, view.toScreenY(-10), Epsilon);
        assertEquals(0, view.toScreenY(10), Epsilon);
        assertEquals(100, view.toScreenX(0), Epsilon);
        assertEquals(50, view.toScreenY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
    @Test
    public void testView3()
    {
        AbstractView view = new AbstractView();
        view.setScreen(200, 100);
        assertFalse(view.isReady());
        view.updatePoint(0, 0);
        view.updatePoint(-10, 10);
        view.updatePoint(10, -10);
        assertEquals(50, view.toScreenX(-10), Epsilon);
        assertEquals(150, view.toScreenX(10), Epsilon);
        assertEquals(100, view.toScreenY(-10), Epsilon);
        assertEquals(0, view.toScreenY(10), Epsilon);
        assertEquals(100, view.toScreenX(0), Epsilon);
        assertEquals(50, view.toScreenY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
    @Test
    public void testView4()
    {
        AbstractView view = new AbstractView();
        view.setScreen(600, 895);
        double d = (895.0-600.0)/2;
        assertFalse(view.isReady());
        view.updatePoint(-13.60272896379027,28.131008962509526);
        assertEquals(-13.60272896379027, view.xMin, Epsilon);
        assertEquals(-13.60272896379027, view.xMax, Epsilon);
        assertEquals(28.131008962509526, view.yMin, Epsilon);
        assertEquals(28.131008962509526, view.yMax, Epsilon);
        view.updatePoint(-13.602733673016058,28.130998989573197);
        assertEquals(-13.602733673016058, view.xMin, Epsilon);
        assertEquals(-13.60272896379027, view.xMax, Epsilon);
        assertEquals(28.130998989573197, view.yMin, Epsilon);
        assertEquals(28.131008962509526, view.yMax, Epsilon);
    }
    
    @Test
    public void testView5()
    {
        AbstractView view = new AbstractView(-1, 2, -2, 10);
        view.setScreen(7, 12);
        assertTrue(view.isReady());
        assertEquals(2, view.toScreenX(-1), Epsilon);
        assertEquals(5, view.toScreenX(2), Epsilon);
        assertEquals(12, view.toScreenY(-2), Epsilon);
        assertEquals(0, view.toScreenY(10), Epsilon);
        assertEquals(3, view.toScreenX(0), Epsilon);
        assertEquals(10, view.toScreenY(0), Epsilon);
        assertEquals(1, view.scale(1), Epsilon);
    }

    @Test
    public void testView6()
    {
        AbstractView view = new AbstractView();
        view.setScreen(200, 100);
        assertFalse(view.isReady());
        view.updateCircle(0, 0, 10);
        assertEquals(50, view.toScreenX(-10), Epsilon);
        assertEquals(150, view.toScreenX(10), Epsilon);
        assertEquals(100, view.toScreenY(-10), Epsilon);
        assertEquals(0, view.toScreenY(10), Epsilon);
        assertEquals(100, view.toScreenX(0), Epsilon);
        assertEquals(50, view.toScreenY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
}
