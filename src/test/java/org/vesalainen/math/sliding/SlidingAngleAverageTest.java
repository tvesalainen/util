/*
 * Copyright (C) 2016 tkv
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

import org.vesalainen.math.sliding.SlidingAngleAverage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class SlidingAngleAverageTest
{
    private static final double Epsilon = 1e-10;
    
    public SlidingAngleAverageTest()
    {
    }

    @Test
    public void test()
    {
        SlidingAngleAverage saa = new SlidingAngleAverage(3);
        saa.add(338);
        assertEquals(338, saa.fast(), Epsilon);
        assertEquals(saa.average(), saa.fast(), Epsilon);
    }
    
}
