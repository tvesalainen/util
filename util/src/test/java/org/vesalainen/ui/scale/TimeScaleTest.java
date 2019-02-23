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
import static java.util.concurrent.TimeUnit.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeScaleTest
{
    
    public TimeScaleTest()
    {
    }

    @Test
    public void test1()
    {
        TimeScale ts = new TimeScale();
        Iterator<ScaleLevel> iterator = ts.iterator(DAYS.toSeconds(10));
        ScaleLevel next = iterator.next();
        assertTrue(next.step() <= DAYS.toSeconds(10));
        assertEquals(DAYS.toSeconds(1), next.step(), 1e-10);;
        assertEquals("12d", next.label(Locale.US, DAYS.toSeconds(12)));
        next = iterator.next();
        assertEquals(HOURS.toSeconds(1), next.step(), 1e-10);;
        assertEquals("23h", next.label(Locale.US, HOURS.toSeconds(23)));
        next = iterator.next();
        assertEquals(MINUTES.toSeconds(1), next.step(), 1e-10);;
        assertEquals("23m", next.label(Locale.US, MINUTES.toSeconds(23)));
        next = iterator.next();
        assertEquals(1, next.step(), 1e-10);;
        assertEquals("3s", next.label(Locale.US, SECONDS.toSeconds(3)));
        next = iterator.next();
        assertEquals(0.5, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.1, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.05, next.step(), 1e-10);;
        assertEquals("1.54s", next.label(Locale.US, 1.5432));
    }
    @Test
    public void test2()
    {
        TimeScale ts = new TimeScale();
        Iterator<ScaleLevel> iterator = ts.iterator(MINUTES.toSeconds(60));
        ScaleLevel next = iterator.next();
        assertTrue(next.step() <= MINUTES.toSeconds(60));
        assertEquals(HOURS.toSeconds(1), next.step(), 1e-10);;
        assertEquals("23h", next.label(Locale.US, HOURS.toSeconds(23)));
        next = iterator.next();
        assertEquals(MINUTES.toSeconds(1), next.step(), 1e-10);;
        assertEquals("23m", next.label(Locale.US, MINUTES.toSeconds(23)));
        next = iterator.next();
        assertEquals(1, next.step(), 1e-10);;
        assertEquals("3s", next.label(Locale.US, SECONDS.toSeconds(3)));
        next = iterator.next();
        assertEquals(0.5, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.1, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.05, next.step(), 1e-10);;
        assertEquals("1.54s", next.label(Locale.US, 1.5432));
    }
    @Test
    public void test3()
    {
        TimeScale ts = new TimeScale();
        Iterator<ScaleLevel> iterator = ts.iterator(23.45);
        ScaleLevel next = iterator.next();
        assertEquals(1, next.step(), 1e-10);;
        assertEquals("3s", next.label(Locale.US, SECONDS.toSeconds(3)));
        next = iterator.next();
        assertEquals(0.5, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.1, next.step(), 1e-10);;
        assertEquals("1.5s", next.label(Locale.US, 1.5432));
        next = iterator.next();
        assertEquals(0.05, next.step(), 1e-10);;
        assertEquals("1.54s", next.label(Locale.US, 1.5432));
    }
    @Test
    public void test4()
    {
        double v = DAYS.toSeconds(123);
        v += HOURS.toSeconds(4);
        v += MINUTES.toSeconds(56);
        v += 7.89;
        TimeScale ts = new TimeScale();
        Iterator<ScaleLevel> iterator = ts.iterator(DAYS.toSeconds(10));
        ScaleLevel next = iterator.next();
        assertEquals(DAYS.toSeconds(1), next.step(), 1e-10);;
        assertEquals("123d", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(HOURS.toSeconds(1), next.step(), 1e-10);;
        assertEquals("123d4h", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(MINUTES.toSeconds(1), next.step(), 1e-10);;
        assertEquals("123d4h56m", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(1, next.step(), 1e-10);;
        assertEquals("123d4h56m8s", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(0.5, next.step(), 1e-10);;
        assertEquals("123d4h56m7.9s", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(0.1, next.step(), 1e-10);;
        assertEquals("123d4h56m7.9s", next.label(Locale.US, v));
        next = iterator.next();
        assertEquals(0.05, next.step(), 1e-10);;
        assertEquals("123d4h56m7.89s", next.label(Locale.US, v));
    }
}
