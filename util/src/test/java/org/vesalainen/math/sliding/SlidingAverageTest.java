/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.sliding;

import java.util.stream.DoubleStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingAverageTest()
    {
    }

    @Test
    public void test0()
    {
        assertEquals(1, (Integer.MAX_VALUE+1) - Integer.MAX_VALUE);
    }
    @Test
    public void test1()
    {
        DoubleSlidingAverage sa = new DoubleSlidingAverage(2);
        sa.accept(1);
        assertEquals(1, sa.last(), Epsilon);
        assertEquals(1, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        sa.accept(3);
        assertEquals(3, sa.last(), Epsilon);
        assertEquals(1, sa.previous(), Epsilon);
        assertEquals(2, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        sa.accept(5);
        assertEquals(5, sa.last(), Epsilon);
        assertEquals(3, sa.previous(), Epsilon);
        assertEquals(4, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        sa.accept(7);
        assertEquals(6, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
    }
    
    @Test
    public void test2()
    {
        DoubleSlidingAverage sa = new DoubleSlidingAverage(2);
        DoubleStream ds = DoubleStream.of(1, 3, 5, 7);
        ds.forEach(sa);
        assertEquals(6, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        assertEquals(6, sa.stream().average().getAsDouble(), Epsilon);
    }
    
    @Test
    public void test3()
    {
        DoubleSlidingAverage sa = new DoubleSlidingAverage(3);
        DoubleStream ds = DoubleStream.of(1, 3, 5, 7);
        ds.forEach(sa);
        assertEquals(5, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        assertEquals(5, sa.stream().average().getAsDouble(), Epsilon);
    }
    //@Test // takes about 2419 s
    public void testOverflow()
    {
        DoubleSlidingAverage sa = new DoubleSlidingAverage(10);
        for (int ii=1;ii<=10;ii++)
        {
            sa.accept(ii);
        }
        assertEquals((double)(sum(10))/10, sa.fast(), 1e-10);
        for (long l=11L;l<0x100000000L;l++)
        {
            sa.accept(l);
            double exp = (double)(sum(l)-sum(l-10))/10;
            if (exp < 0)
            {
                break;
            }
            double ulp = Math.ulp(exp);
            assertEquals("l="+l+" ulp="+ulp+" delta="+delta(exp, sa.fast()), exp, sa.fast(), ulp);
        }
    }
    private static long sum(long v)
    {
        return (v*v+v)/2L;
    }
    private static double delta(double a, double b)
    {
        return a-b;
    }
}
