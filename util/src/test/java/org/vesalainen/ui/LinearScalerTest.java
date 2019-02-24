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

import java.awt.Font;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen
 */
public class LinearScalerTest
{
    private static final double Epsilon = 1e-10;
    
    public LinearScalerTest()
    {
    }

    @Test
    public void test1()
    {
        LinearScaler sc = new LinearScaler(115, 144);
        assertEquals(-0.5, sc.getLevelFor(new Font("ariel", 0, 5), true), Epsilon);
        assertEquals(-0.5, sc.getLevelFor(new Font("ariel", 0, 5), false), Epsilon);
        assertEquals(0.5, sc.level(), Epsilon);
        assertEquals(10, sc.step(0), Epsilon);
        assertEquals(5, sc.step(0.5), Epsilon);
        assertEquals(1, sc.step(1), Epsilon);
        assertEquals("%.0f", sc.getFormat(0));
        assertEquals("%.0f", sc.getFormat(1));
        assertEquals("%.1f", sc.getFormat(2));
        assertEquals(3, sc.count(0), Epsilon);
        PrimitiveIterator.OfDouble i0 = Spliterators.iterator(sc.spliterator(0));
        assertEquals(120, i0.nextDouble(), Epsilon);
        assertEquals(130, i0.nextDouble(), Epsilon);
        assertEquals(140, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        PrimitiveIterator.OfDouble i05 = Spliterators.iterator(sc.spliterator(0.5));
        assertEquals(115, i05.nextDouble(), Epsilon);
        assertEquals(120, i05.nextDouble(), Epsilon);
        assertEquals(125, i05.nextDouble(), Epsilon);
        assertEquals(130, i05.nextDouble(), Epsilon);
        assertEquals(135, i05.nextDouble(), Epsilon);
        assertEquals(140, i05.nextDouble(), Epsilon);
        assertFalse(i05.hasNext());
        assertEquals(6, sc.count(0.5), Epsilon);
        PrimitiveIterator.OfDouble i1 = Spliterators.iterator(sc.spliterator(1));
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
        PrimitiveIterator.OfDouble i2 = Spliterators.iterator(sc.spliterator(2));
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
        LinearScaler sc = new LinearScaler(0, 30);
        PrimitiveIterator.OfDouble i0 = Spliterators.iterator(sc.spliterator(0));
        assertEquals(0, i0.nextDouble(), Epsilon);
        assertEquals(10, i0.nextDouble(), Epsilon);
        assertEquals(20, i0.nextDouble(), Epsilon);
        assertEquals(30, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        assertEquals(4, sc.count(0), Epsilon);
        PrimitiveIterator.OfDouble i5 = Spliterators.iterator(sc.spliterator(0.5));
        assertEquals(0, i5.nextDouble(), Epsilon);
        assertEquals(5, i5.nextDouble(), Epsilon);
        assertEquals(10, i5.nextDouble(), Epsilon);
        assertEquals(15, i5.nextDouble(), Epsilon);
        assertEquals(20, i5.nextDouble(), Epsilon);
        assertEquals(25, i5.nextDouble(), Epsilon);
        assertEquals(30, i5.nextDouble(), Epsilon);
        assertFalse(i5.hasNext());
        assertEquals(7, sc.count(0.5), Epsilon);
    }

    @Test
    public void test3()
    {
        LinearScaler sc = new LinearScaler(0.001234, 0.00678);
        PrimitiveIterator.OfDouble i0 = Spliterators.iterator(sc.spliterator(0));
        assertEquals(0.002, i0.nextDouble(), Epsilon);
        assertEquals(0.003, i0.nextDouble(), Epsilon);
        assertEquals(0.004, i0.nextDouble(), Epsilon);
        assertEquals(0.005, i0.nextDouble(), Epsilon);
        assertEquals(0.006, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        assertEquals(5, sc.count(0), Epsilon);
    }

    @Test
    public void test4()
    {
        List<String> exp = CollectionHelp.create("0.002", "0.003", "0.004", "0.005", "0.006");
        LinearScaler sc = new LinearScaler(0.001234, 0.00678);
        assertEquals(exp, sc.getLabels(Locale.US, 0));
    }
    
    @Test
    public void test5()
    {
        List<String> exp = CollectionHelp.create("1000", "2000", "3000", "4000", "5000");
        LinearScaler sc = new LinearScaler(100, 5200);
        assertEquals(exp, sc.getLabels(Locale.US, 0));
        assertEquals(1.0, sc.getLevelFor(new Font("ariel", 0, 5), true), Epsilon);
        assertEquals(1.5, sc.getLevelFor(new Font("ariel", 0, 5), false), Epsilon);
    }
    @Test
    public void test6()
    {
        LinearScaler sc = new LinearScaler(0, 4);
        long count = sc.stream().count();
        assertEquals(5, count);
    }
    @Test
    public void test7()
    {
        LinearScaler sc = new LinearScaler(-1, 19);
        assertEquals(4, sc.count(0.5), Epsilon);
        PrimitiveIterator.OfDouble i0 = Spliterators.iterator(sc.spliterator(0.5));
        assertEquals(0, i0.nextDouble(), Epsilon);
        assertEquals(5, i0.nextDouble(), Epsilon);
        assertEquals(10, i0.nextDouble(), Epsilon);
        assertEquals(15, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
    }
    @Test
    public void test8()
    {
        LinearScaler sc = new LinearScaler(3, 3.5);
        sc.calc();
        assertEquals("%.1f", sc.getFormat(0));
        assertEquals("%.1f", sc.getFormat(0.5));
    }
}