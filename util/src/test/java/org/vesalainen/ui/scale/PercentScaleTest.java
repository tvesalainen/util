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
        //PercentScale ds = new PercentScale(10);
        //Tester.generator(ds, 0, 20);
    }

    @Test
    public void testAuto()
    {
        ScaleLevel level;
        Tester t;
        PercentScale scale = new PercentScale(10);
        Iterator<ScaleLevel> iterator = scale.iterator(0.0, 20.0);
        t = new Tester();
        t.add(0.0, "0%");
        t.add(10.0, "100%");
        t.add(20.0, "200%");
        level = iterator.next();
        level.forEach(0.0, 20.0, Locale.US, t::check);
        t = new Tester();
        t.add(0.0, "0%");
        t.add(5.0, "50%");
        t.add(10.0, "100%");
        t.add(15.0, "150%");
        t.add(20.0, "200%");
        level = iterator.next();
        level.forEach(0.0, 20.0, Locale.US, t::check);
        t = new Tester();
        t.add(0.0, "0%");
        t.add(1.0, "10%");
        t.add(2.0, "20%");
        t.add(2.9999999999999996, "30%");
        t.add(4.0, "40%");
        t.add(5.0, "50%");
        t.add(5.999999999999999, "60%");
        t.add(6.999999999999999, "70%");
        t.add(8.0, "80%");
        t.add(9.0, "90%");
        t.add(10.0, "100%");
        t.add(11.0, "110%");
        t.add(11.999999999999998, "120%");
        t.add(13.0, "130%");
        t.add(13.999999999999998, "140%");
        t.add(15.0, "150%");
        t.add(16.0, "160%");
        t.add(17.0, "170%");
        t.add(18.0, "180%");
        t.add(18.999999999999996, "190%");
        t.add(20.0, "200%");
        level = iterator.next();
        level.forEach(0.0, 20.0, Locale.US, t::check);
    }

    @Test
    public void test0()
    {
        PercentScale ps = new PercentScale(20.0);
        Iterator<ScaleLevel> i1 = ps.iterator(0, 20);

        ScaleLevel next = i1.next();
        next.forEach(0, 20, Locale.US, (d, l) -> System.err.println(d + " " + l));
        assertEquals("25%", next.label(Locale.US, 5));
        PrimitiveIterator.OfDouble i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(20, i2.nextDouble(), 1e-10);

        next = i1.next();
        next.forEach(0, 20, Locale.US, (d, l) -> System.err.println(d + " " + l));
        assertEquals("25%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(10, i2.nextDouble(), 1e-10);
        assertEquals(20, i2.nextDouble(), 1e-10);

        next = i1.next();
        assertEquals("25%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(2, i2.nextDouble(), 1e-10);

        next = i1.next();
        assertEquals("25%", next.label(Locale.US, 5));
        i2 = next.iterator(0, 20);
        assertEquals(0, i2.nextDouble(), 1e-10);
        assertEquals(1, i2.nextDouble(), 1e-10);
    }

    @Test
    public void test1()
    {
        PercentScale ps = new PercentScale(10);
        Iterator<ScaleLevel> i1 = ps.iterator(0, 20);
        ScaleLevel next = i1.next();
        next.forEach(0, 20, Locale.US, (d, l) -> System.err.println(d + " " + l));
    }

}
