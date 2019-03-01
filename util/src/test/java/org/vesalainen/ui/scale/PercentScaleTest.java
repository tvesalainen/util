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
public class PercentScaleTest
{
    
    public PercentScaleTest()
    {
    }

    @Test
    public void test0()
    {
        PercentScale ps = new PercentScale(20.0);
        Iterator<ScaleLevel> i1 = ps.iterator(0, 20);
        
        ScaleLevel next = i1.next();
        next.forEach(0, 20, Locale.US, (d,l)->System.err.println(d+" "+l));
        assertEquals("25%", next.label(Locale.US, 5));
        PrimitiveIterator.OfDouble i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        //assertEquals(1, i2.nextDouble(), 1e-10);
        
        next = i1.next();
        next.forEach(0, 20, Locale.US, (d,l)->System.err.println(d+" "+l));
        assertEquals("25%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(10, i2.nextDouble(), 1e-10);
        assertEquals(20, i2.nextDouble(), 1e-10);
        
        next = i1.next();
        assertEquals("25%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(1, i2.nextDouble(), 1e-10);
        
        next = i1.next();
        assertEquals("25.0%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(0.1, i2.nextDouble(), 1e-10);
    }
    @Test
    public void test1()
    {
        PercentScale ps = new PercentScale(10, 1);
        Iterator<ScaleLevel> i1 = ps.iterator(0, 20);
        ScaleLevel next = i1.next();
        next.forEach(0, 20, Locale.US, (d,l)->System.err.println(d+" "+l));
    }
    
}
