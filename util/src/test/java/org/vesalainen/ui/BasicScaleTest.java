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
package org.vesalainen.ui;

import java.util.Iterator;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicScaleTest
{
    
    public BasicScaleTest()
    {
    }

    @Test
    public void test1()
    {
        BasicScale bs = new BasicScale();
        assertEquals(1.0, bs.closest(1).step(), 1e-10);
        assertEquals(0.1, bs.closest(0.999999).step(), 1e-10);
        ScaleLevel sl = bs.closest(1);
        assertEquals("3", sl.label(Locale.US, Math.PI));
        sl = sl.next();
        assertEquals("3.1", sl.label(Locale.US, Math.PI));
    }
    @Test
    public void testIterator()
    {
        BasicScale bs = new BasicScale();
        ScaleLevel sl = bs.closest(1);
        Iterator<ScaleLevel> iterator = sl.iterator();
        assertEquals(1.0, iterator.next().step(), 1e-10);
        assertEquals(0.1, iterator.next().step(), 1e-10);
        assertEquals(0.01, iterator.next().step(), 1e-10);
    }
    @Test
    public void test5()
    {
        BasicScale bs = new BasicScale(5);
        assertEquals(0.5, bs.closest(1).step(), 1e-10);
        assertEquals(0.5, bs.closest(0.999999).step(), 1e-10);
        ScaleLevel sl = bs.closest(1);
        sl = sl.next();
        assertEquals(0.05, sl.step(), 1e-10);
    }
    @Test
    public void test1_5()
    {
        BasicScale bs1 = new BasicScale();
        BasicScale bs3 = new BasicScale(3);
        BasicScale bs5 = new BasicScale(5);
        
        Iterator<ScaleLevel> iterator = Scale.merge(1, bs1, bs3, bs5);
        assertEquals(1.0, iterator.next().step(), 1e-10);
        assertEquals(0.5, iterator.next().step(), 1e-10);
        assertEquals(0.3, iterator.next().step(), 1e-10);
        assertEquals(0.1, iterator.next().step(), 1e-10);
        assertEquals(0.05, iterator.next().step(), 1e-10);
        assertEquals(0.03, iterator.next().step(), 1e-10);
        assertEquals(0.01, iterator.next().step(), 1e-10);
    }    
}
