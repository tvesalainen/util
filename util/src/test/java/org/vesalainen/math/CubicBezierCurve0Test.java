/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class CubicBezierCurve0Test
{
    static final double Epsilon = 1e-6;
    public CubicBezierCurve0Test()
    {
    }

    @Test
    public void test1()
    {
        CubicBezierCurve0 cbc = new CubicBezierCurve0(0, 2, 1, 3, 2, 3, 3, 4);
        Point p = cbc.eval(0.3);
        assertEquals(0.9, p.getX(), Epsilon);
        assertEquals(2.684, p.getY(), Epsilon);
        p = cbc.eval(0.6);
        assertEquals(1.8, p.getX(), Epsilon);
        assertEquals(3.152, p.getY(), Epsilon);
    }
    
}
