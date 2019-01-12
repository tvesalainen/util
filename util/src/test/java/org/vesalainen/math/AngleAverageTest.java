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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleAverageTest
{
    
    public AngleAverageTest()
    {
    }

    @Test
    public void test1()
    {
        AngleAverage aa = new AngleAverage();
        aa.add(Math.PI);
        assertEquals(Math.PI, aa.average(), 1e-10);
    }
    @Test
    public void test2()
    {
        AngleAverage aa = new AngleAverage();
        aa.addDeg(360);
        aa.addDeg(0);
        assertEquals(0, aa.averageDeg(), 1e-10);
    }
    @Test
    public void test3()
    {
        AngleAverage aa = new AngleAverage();
        aa.addDeg(350);
        aa.addDeg(10);
        assertEquals(0, aa.averageDeg(), 1e-10);
    }
    
}
