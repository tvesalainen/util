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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateScaleTest
{
    private static final double MIN = 1.0/60.0;
    public CoordinateScaleTest()
    {
    }

    @Test
    public void testLat()
    {
        Scale cs = CoordinateScale.LATITUDE;
        Iterator<ScaleLevel> iterator = cs.iterator(0, 20);
        ScaleLevel next = iterator.next();
        assertTrue(next.step() <= 20);
        assertEquals(10, next.step(), 1e-10);
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        assertEquals("S60\u00B0", next.label(Locale.US, -60.5));
        next = iterator.next();
        assertEquals(5, next.step(), 1e-10);
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals(1, next.step(), 1e-10);
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals(10*MIN, next.step(), 1e-10);
        assertEquals("N60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals(MIN, next.step(), 1e-10);
        assertEquals("N60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals(MIN/10.0, next.step(), 1e-10);
        assertEquals("N60\u00B030.0'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals(MIN/100.0, next.step(), 1e-10);
        assertEquals("N60\u00B030.00'", next.label(Locale.US, 60.5));
        assertEquals("S60\u00B030.00'", next.label(Locale.US, -60.5));
    }
    @Test
    public void testLon()
    {
        Scale cs = CoordinateScale.LONGITUDE;
        Iterator<ScaleLevel> iterator = cs.iterator(0, 20);
        ScaleLevel next = iterator.next();
        assertEquals("E60\u00B0", next.label(Locale.US, 60.5));
        assertEquals("W60\u00B0", next.label(Locale.US, -60.5));
        next = iterator.next();
        assertEquals("E60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("E60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("E60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("E60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("E60\u00B030.0'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("E60\u00B030.00'", next.label(Locale.US, 60.5));
        assertEquals("W60\u00B030.00'", next.label(Locale.US, -60.5));
    }
    
}
