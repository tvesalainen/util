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
    private static final double Epsilon = 0.000001F;
    
    public AbstractViewTest()
    {
    }

    @Test
    public void testView1()
    {
        AbstractView view = new AbstractView(-10, 10, -10, 10);
        view.setScreen(100, 200);
        assertTrue(view.isReady());
        assertEquals(0, view.translateX(-10), Epsilon);
        assertEquals(100, view.translateX(10), Epsilon);
        assertEquals(150, view.translateY(-10), Epsilon);
        assertEquals(50, view.translateY(10), Epsilon);
        assertEquals(50, view.translateX(0), Epsilon);
        assertEquals(100, view.translateY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
    @Test
    public void testView2()
    {
        AbstractView view = new AbstractView(-10, 10, -10, 10);
        view.setScreen(200, 100);
        assertTrue(view.isReady());
        assertEquals(50, view.translateX(-10), Epsilon);
        assertEquals(150, view.translateX(10), Epsilon);
        assertEquals(100, view.translateY(-10), Epsilon);
        assertEquals(0, view.translateY(10), Epsilon);
        assertEquals(100, view.translateX(0), Epsilon);
        assertEquals(50, view.translateY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
    @Test
    public void testView3()
    {
        AbstractView view = new AbstractView();
        view.setScreen(200, 100);
        assertFalse(view.isReady());
        view.update(0, 0);
        view.update(-10, 10);
        view.update(10, -10);
        assertEquals(50, view.translateX(-10), Epsilon);
        assertEquals(150, view.translateX(10), Epsilon);
        assertEquals(100, view.translateY(-10), Epsilon);
        assertEquals(0, view.translateY(10), Epsilon);
        assertEquals(100, view.translateX(0), Epsilon);
        assertEquals(50, view.translateY(0), Epsilon);
        assertEquals(5, view.scale(1), Epsilon);
    }
    
}
