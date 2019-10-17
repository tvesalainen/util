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
package org.vesalainen.time;

import java.time.Instant;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MutableInstantTest
{
    
    public MutableInstantTest()
    {
    }

    @Test
    public void test1()
    {
        MutableInstant mi = new MutableInstant(10, -1);
        assertEquals(9, mi.second());
        assertEquals(999999999L, mi.nano());
    }
    @Test
    public void test2()
    {
        MutableInstant mi = new MutableInstant(10, 1000000000);
        assertEquals(11, mi.second());
        assertEquals(0L, mi.nano());
    }
    @Test
    public void testPlus()
    {
        MutableInstant mi = MutableInstant.now();
        Instant exp = mi.instant();
        assertEquals(exp.toEpochMilli(), mi.millis());
        assertTrue(mi.isEqual(exp));
        long v = 12345678912345L;
        mi.plus(v);
        assertTrue(mi.isEqual(exp.plusNanos(v)));
    }
    @Test
    public void testUntil()
    {
        long v = 12345678912345L;
        MutableInstant mi1 = MutableInstant.now();
        MutableInstant mi2 = new MutableInstant(mi1);
        assertTrue(mi1.compareTo(mi2)==0);
        mi2.plus(v);
        assertTrue(mi1.compareTo(mi2)<0);
        assertTrue(mi2.compareTo(mi1)>0);
        assertEquals(v, mi1.until(mi2));
        mi2.plus(-v);
        assertEquals(mi1, mi2);
    }
    @Test
    public void testNanoTime()
    {
        MutableInstant mi1 = MutableInstant.now();
        MutableInstant mi2 = new MutableInstant(0, System.nanoTime());
        long d = mi1.until(mi2);
        mi2.plus(-d);
        assertEquals(mi1, mi2);
    }    
}
