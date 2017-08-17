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
    public void test1()
    {
        SlidingAverage sa = new SlidingAverage(2);
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
        SlidingAverage sa = new SlidingAverage(2);
        DoubleStream ds = DoubleStream.of(1, 3, 5, 7);
        ds.forEach(sa);
        assertEquals(6, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        assertEquals(6, sa.stream().average().getAsDouble(), Epsilon);
    }
    
    @Test
    public void test3()
    {
        SlidingAverage sa = new SlidingAverage(3);
        DoubleStream ds = DoubleStream.of(1, 3, 5, 7);
        ds.forEach(sa);
        assertEquals(5, sa.fast(), Epsilon);
        assertEquals(sa.fast(), sa.average(), Epsilon);
        assertEquals(5, sa.stream().average().getAsDouble(), Epsilon);
    }
    
}
