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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingAngleStatsTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingAngleStatsTest()
    {
    }

    @Test
    public void test1()
    {
        SlidingAngleStats t = new SlidingAngleStats(3);
        t.accept(30);
        assertEquals(30, t.fast(), Epsilon);
        assertEquals(30, t.average(), Epsilon);
        assertEquals(30, t.getMax(), Epsilon);
        assertEquals(30, t.getMin(), Epsilon);
        t.accept(40);
        assertEquals(35, t.fast(), Epsilon);
        assertEquals(35, t.average(), Epsilon);
        assertEquals(40, t.getMax(), Epsilon);
        assertEquals(30, t.getMin(), Epsilon);
        t.accept(350);
        assertEquals(40, t.getMax(), Epsilon);
        assertEquals(350, t.getMin(), Epsilon);
    }
    
    @Test
    public void test2()
    {
        SlidingAngleStats t = new SlidingAngleStats(3);
        t.accept(170);
        assertEquals(170, t.fast(), Epsilon);
        assertEquals(170, t.average(), Epsilon);
        assertEquals(170, t.getMax(), Epsilon);
        assertEquals(170, t.getMin(), Epsilon);
        t.accept(190);
        assertEquals(180, t.fast(), Epsilon);
        assertEquals(180, t.average(), Epsilon);
        assertEquals(190, t.getMax(), Epsilon);
        assertEquals(170, t.getMin(), Epsilon);
        t.accept(160);
        assertEquals(190, t.getMax(), Epsilon);
        assertEquals(160, t.getMin(), Epsilon);
    }
    
}
