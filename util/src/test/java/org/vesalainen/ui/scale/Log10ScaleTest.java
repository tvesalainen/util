/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ui.scale;

import java.util.Iterator;
import java.util.Locale;
import java.util.PrimitiveIterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Log10ScaleTest
{
    
    public Log10ScaleTest()
    {
    }

    @Test
    public void test1()
    {
        Log10Scale ls = new Log10Scale();
        Iterator<ScaleLevel> i1 = ls.iterator(20, 30);
        ScaleLevel next = i1.next();
        
        assertEquals("2x10¹", next.label(Locale.US, 20));
        assertEquals("3x10¹", next.label(Locale.US, 30));
                
        PrimitiveIterator.OfDouble i2 = next.iterator(20, 30);
        assertEquals(20, i2.nextDouble(), 1e-10);
        assertEquals(30, i2.nextDouble(), 1e-10);
        assertFalse(i2.hasNext());
    }
    @Test
    public void test2()
    {
        Log10Scale ls = new Log10Scale();
        Iterator<ScaleLevel> i1 = ls.iterator(20, 20000);
        ScaleLevel next = i1.next();
        
        assertEquals("10²", next.label(Locale.US, 100));
        
        PrimitiveIterator.OfDouble i2 = next.iterator(20, 20000);
        assertEquals(100, i2.nextDouble(), 1e-10);
        assertEquals(1000, i2.nextDouble(), 1e-10);
        assertEquals(10000, i2.nextDouble(), 1e-10);
        assertFalse(i2.hasNext());
        
        next = i1.next();
        i2 = next.iterator(20, 20000);
        assertEquals(20, i2.nextDouble(), 1e-10);
        assertEquals(30, i2.nextDouble(), 1e-10);
        assertEquals(40, i2.nextDouble(), 1e-10);
        assertEquals(50, i2.nextDouble(), 1e-10);
        assertEquals(60, i2.nextDouble(), 1e-10);
        assertEquals(70, i2.nextDouble(), 1e-10);
        assertEquals(80, i2.nextDouble(), 1e-10);
        assertEquals(90, i2.nextDouble(), 1e-10);
        assertEquals(100, i2.nextDouble(), 1e-10);
        assertEquals(200, i2.nextDouble(), 1e-10);
        assertEquals(300, i2.nextDouble(), 1e-10);
        assertEquals(400, i2.nextDouble(), 1e-10);
        assertEquals(500, i2.nextDouble(), 1e-10);
        assertEquals(600, i2.nextDouble(), 1e-10);
        assertEquals(700, i2.nextDouble(), 1e-10);
        assertEquals(800, i2.nextDouble(), 1e-10);
        assertEquals(900, i2.nextDouble(), 1e-10);
        assertEquals(1000, i2.nextDouble(), 1e-10);
        assertEquals(2000, i2.nextDouble(), 1e-10);
        assertEquals(3000, i2.nextDouble(), 1e-10);
        assertEquals(4000, i2.nextDouble(), 1e-10);
        assertEquals(5000, i2.nextDouble(), 1e-10);
        assertEquals(6000, i2.nextDouble(), 1e-10);
        assertEquals(7000, i2.nextDouble(), 1e-10);
        assertEquals(8000, i2.nextDouble(), 1e-10);
        assertEquals(9000, i2.nextDouble(), 1e-10);
        assertEquals(10000, i2.nextDouble(), 1e-10);
        assertEquals(20000, i2.nextDouble(), 1e-10);
        assertFalse(i2.hasNext());
        
    }
    
}
