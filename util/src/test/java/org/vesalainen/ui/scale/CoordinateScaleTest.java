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
    
    public CoordinateScaleTest()
    {
    }

    @Test
    public void testLat()
    {
        Scale cs = CoordinateScale.LATITUDE;
        Iterator<ScaleLevel> iterator = cs.iterator(20);
        ScaleLevel next = iterator.next();
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        assertEquals("S60\u00B0", next.label(Locale.US, -60.5));
        next = iterator.next();
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("N60\u00B0", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("N60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("N60\u00B030'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("N60\u00B030.0'", next.label(Locale.US, 60.5));
        next = iterator.next();
        assertEquals("N60\u00B030.00'", next.label(Locale.US, 60.5));
        assertEquals("S60\u00B030.00'", next.label(Locale.US, -60.5));
    }
    @Test
    public void testLon()
    {
        Scale cs = CoordinateScale.LONGITUDE;
        Iterator<ScaleLevel> iterator = cs.iterator(20);
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
