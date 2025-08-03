/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingAverageTest
{
    
    public DoubleTimeoutSlidingAverageTest()
    {
    }

    //@Test
    public void test1()
    {
        AtomicInteger time = new AtomicInteger();
        DoubleTimeoutSlidingAverage ave = new DoubleTimeoutSlidingAverage(8, 3);
        Random random = new Random(12345678L);
        for (int ii=0;ii<1000000;ii++)
        {
            double d = random.nextDouble();
            ave.accept(d);
            time.addAndGet(1);
            if (Math.abs(ave.average()-ave.fast())>1e-10)
            {
                System.err.println();
                double average = ave.average();
                double fast = ave.fast();
                System.err.println();
            }
            //System.err.println("II="+ii+" D="+d);
            assertEquals("ii="+ii, ave.average(), ave.fast(), 1e-10);
        }
    }
    
}
