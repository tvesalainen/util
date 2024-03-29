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
package org.vesalainen.math;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AverageSeekerTest
{
    
    public AverageSeekerTest()
    {
    }

    @Test
    public void test1()
    {
        AverageSeeker as = new AverageSeeker(64, 1e-3, ()->System.err.println("done"));
        Random rand = new Random(1234L);
        int count = 0;
        while (!as.isWithin(1e-3))
        {
            as.add(rand.nextGaussian());
            count++;
        }
        assertEquals(20967, count);
    }
    
}
