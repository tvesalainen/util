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

import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen
 */
public class ScalerTest
{
    private static final double Epsilon = 1e-10;
    
    public ScalerTest()
    {
    }

    @Test
    public void test1()
    {
        Scaler sc = new Scaler(115, 144);
        PrimitiveIterator.OfDouble i0 = sc.iterator(0);
        assertEquals(120, i0.nextDouble(), Epsilon);
        assertEquals(130, i0.nextDouble(), Epsilon);
        assertEquals(140, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        PrimitiveIterator.OfDouble i1 = sc.iterator(1);
        assertEquals(115, i1.nextDouble(), Epsilon);
        assertEquals(116, i1.nextDouble(), Epsilon);
        assertEquals(117, i1.nextDouble(), Epsilon);
        assertEquals(118, i1.nextDouble(), Epsilon);
        assertEquals(119, i1.nextDouble(), Epsilon);
        double last = 0;
        while (i1.hasNext())
        {
            last = i1.nextDouble();
        }
        assertEquals(144, last, Epsilon);
        PrimitiveIterator.OfDouble i2 = sc.iterator(2);
        assertEquals(115.0, i2.nextDouble(), Epsilon);
        assertEquals(115.1, i2.nextDouble(), Epsilon);
        assertEquals(115.2, i2.nextDouble(), Epsilon);
        assertEquals(115.3, i2.nextDouble(), Epsilon);
        while (i2.hasNext())
        {
            last = i2.nextDouble();
        }
        assertEquals(144, last, Epsilon);
    }

    @Test
    public void test2()
    {
        Scaler sc = new Scaler(0, 30);
        PrimitiveIterator.OfDouble i0 = sc.iterator(0);
        assertEquals(0, i0.nextDouble(), Epsilon);
        assertEquals(10, i0.nextDouble(), Epsilon);
        assertEquals(20, i0.nextDouble(), Epsilon);
        assertEquals(30, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        PrimitiveIterator.OfDouble i5 = sc.iterator(0, 0.5);
        assertEquals(0, i5.nextDouble(), Epsilon);
        assertEquals(5, i5.nextDouble(), Epsilon);
        assertEquals(10, i5.nextDouble(), Epsilon);
        assertEquals(15, i5.nextDouble(), Epsilon);
        assertEquals(20, i5.nextDouble(), Epsilon);
        assertEquals(25, i5.nextDouble(), Epsilon);
        assertEquals(30, i5.nextDouble(), Epsilon);
        assertFalse(i5.hasNext());
    }

    @Test
    public void test3()
    {
        Scaler sc = new Scaler(0.001234, 0.00678);
        PrimitiveIterator.OfDouble i0 = sc.iterator(0);
        assertEquals(0.002, i0.nextDouble(), Epsilon);
        assertEquals(0.003, i0.nextDouble(), Epsilon);
        assertEquals(0.004, i0.nextDouble(), Epsilon);
        assertEquals(0.005, i0.nextDouble(), Epsilon);
        assertEquals(0.006, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
    }

    @Test
    public void test4()
    {
        List<String> exp = Lists.create("0.002", "0.003", "0.004", "0.005", "0.006");
        Scaler sc = new Scaler(0.001234, 0.00678);
        assertEquals(exp, sc.getLabels(Locale.US));
    }
    
    @Test
    public void test5()
    {
        List<String> exp = Lists.create("1000", "2000", "3000", "4000", "5000");
        Scaler sc = new Scaler(100, 5200);
        assertEquals(exp, sc.getLabels(Locale.US));
    }
}
