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
package org.vesalainen.math.sliding;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SlidingStandardDeviationTest
{
    
    public SlidingStandardDeviationTest()
    {
    }

    @Test
    public void test1()
    {
        SlidingStandardDeviation ssd = new SlidingStandardDeviation(4096);
        Random rand = new Random(12345L);
        for (int ii=0;ii<5000;ii++)
        {
            ssd.accept(rand.nextGaussian());
        }
        assertEquals(1.0, ssd.standardDeviation(), 1e-4);
        assertEquals(1.0, ssd.stdevEstimation(), 1e-3);
    }
    
}
