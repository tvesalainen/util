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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.scale.BasicScale;
import org.vesalainen.ui.scale.ScaleLevel;
import org.vesalainen.util.CollectionHelp;

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
        ScaleLevel l1 = sc.getLevelFor(new Font("ariel", 0, 5), true);
        assertEquals(10, l1.step(), Epsilon);
        ScaleLevel l2 = sc.getLevelFor(new Font("ariel", 0, 5), false);
        assertEquals(10, l2.step(), Epsilon);
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel next = iterator.next();
        PrimitiveIterator.OfDouble i0 = sc.iterator(next);
        assertEquals(120, i0.nextDouble(), Epsilon);
        assertEquals(130, i0.nextDouble(), Epsilon);
        assertEquals(140, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        next = iterator.next();
        PrimitiveIterator.OfDouble i05 = sc.iterator(next);
        assertEquals(115, i05.nextDouble(), Epsilon);
        assertEquals(120, i05.nextDouble(), Epsilon);
        assertEquals(125, i05.nextDouble(), Epsilon);
        assertEquals(130, i05.nextDouble(), Epsilon);
        assertEquals(135, i05.nextDouble(), Epsilon);
        assertEquals(140, i05.nextDouble(), Epsilon);
        assertFalse(i05.hasNext());
        assertEquals(6, sc.count(next), Epsilon);
        next = iterator.next();
        PrimitiveIterator.OfDouble i1 = sc.iterator(next);
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
        next = iterator.next();
        PrimitiveIterator.OfDouble i2 = sc.iterator(next);
        assertEquals(115.0, i2.nextDouble(), Epsilon);
        assertEquals(115.5, i2.nextDouble(), Epsilon);
        assertEquals(116.0, i2.nextDouble(), Epsilon);
        assertEquals(116.5, i2.nextDouble(), Epsilon);
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
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel next = iterator.next();
        PrimitiveIterator.OfDouble i0 = sc.iterator(next);
        assertEquals(0, i0.nextDouble(), Epsilon);
        assertEquals(10, i0.nextDouble(), Epsilon);
        assertEquals(20, i0.nextDouble(), Epsilon);
        assertEquals(30, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        assertEquals(4, sc.count(next), Epsilon);
        next = iterator.next();
        PrimitiveIterator.OfDouble i5 = sc.iterator(next);
        assertEquals(0, i5.nextDouble(), Epsilon);
        assertEquals(5, i5.nextDouble(), Epsilon);
        assertEquals(10, i5.nextDouble(), Epsilon);
        assertEquals(15, i5.nextDouble(), Epsilon);
        assertEquals(20, i5.nextDouble(), Epsilon);
        assertEquals(25, i5.nextDouble(), Epsilon);
        assertEquals(30, i5.nextDouble(), Epsilon);
        assertFalse(i5.hasNext());
        assertEquals(7, sc.count(next), Epsilon);
    }

    @Test
    public void test3()
    {
        Scaler sc = new Scaler(0.001234, 0.00678, BasicScale.SCALE10);
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel next = iterator.next();
        PrimitiveIterator.OfDouble i0 = sc.iterator(next);
        assertEquals(0.002, i0.nextDouble(), Epsilon);
        assertEquals(0.003, i0.nextDouble(), Epsilon);
        assertEquals(0.004, i0.nextDouble(), Epsilon);
        assertEquals(0.005, i0.nextDouble(), Epsilon);
        assertEquals(0.006, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
        assertEquals(5, sc.count(next), Epsilon);
    }

    @Test
    public void test4()
    {
        List<String> exp = CollectionHelp.create("0.002", "0.003", "0.004", "0.005", "0.006");
        Scaler sc = new Scaler(0.001234, 0.00678, BasicScale.SCALE10);
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel next = iterator.next();
        assertEquals(exp, sc.getLabels(Locale.US, next));
    }
    
    @Test
    public void test5()
    {
        List<String> exp = CollectionHelp.create("1000", "2000", "3000", "4000", "5000");
        Scaler sc = new Scaler(100, 5200, BasicScale.SCALE10);
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel l0_0 = iterator.next();
        ScaleLevel l0_5 = iterator.next();
        ScaleLevel l1_0 = iterator.next();
        ScaleLevel l1_5 = iterator.next();
        assertEquals(exp, sc.getLabels(Locale.US, l0_0));
        assertEquals(l0_0, sc.getLevelFor(new Font("ariel", 0, 5), true));
        assertEquals(l0_5, sc.getLevelFor(new Font("ariel", 0, 5), false));
    }
    @Test
    public void test6()
    {
        Scaler sc = new Scaler(0, 4);
        long count = sc.stream().count();
        assertEquals(5, count);
    }
    @Test
    public void test7()
    {
        Scaler sc = new Scaler(-1, 19);
        Iterator<ScaleLevel> iterator = sc.iterator();
        ScaleLevel l0_0 = iterator.next();
        ScaleLevel l0_5 = iterator.next();
        assertEquals(4, sc.count(l0_5), Epsilon);
        PrimitiveIterator.OfDouble i0 = Spliterators.iterator(sc.spliterator(l0_5));
        assertEquals(0, i0.nextDouble(), Epsilon);
        assertEquals(5, i0.nextDouble(), Epsilon);
        assertEquals(10, i0.nextDouble(), Epsilon);
        assertEquals(15, i0.nextDouble(), Epsilon);
        assertFalse(i0.hasNext());
    }
}
