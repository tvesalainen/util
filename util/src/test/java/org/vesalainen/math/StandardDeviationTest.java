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
public class StandardDeviationTest
{
    
    public StandardDeviationTest()
    {
    }

    @Test
    public void test1()
    {
        Random random = new Random(123456L);
        StandardDeviation sd = new StandardDeviation();
        for (int ii=0;ii<1000;ii++)
        {
            sd.accept(random.nextGaussian());
        }
        assertEquals(1.0, sd.getStandardDeviation(), 1e-2);
    }
    
}
