/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class XYSamplesTest
{
    
    public XYSamplesTest()
    {
    }

    @Test
    public void test1()
    {
        XYSamples s = new XYSamples();
        s.add(1, 2);
        s.add(2, 4);
        s.add(3, 6);
        s.add(4, 8);
        s.add(5, 10);
        assertEquals(5, s.getCount());
        assertEquals(3, s.getX(2), 1e-10);
        assertEquals(6, s.getY(2), 1e-10);
        assertEquals(15, s.xStream().sum(), 1e-10);
        assertEquals(30, s.yStream().sum(), 1e-10);
        assertEquals("[(1.0,2.0)(2.0,4.0)(3.0,6.0)(4.0,8.0)(5.0,10.0)]", s.toString());
    }
    
}
