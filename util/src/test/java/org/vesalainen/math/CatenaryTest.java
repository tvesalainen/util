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
public class CatenaryTest
{
    
    public CatenaryTest()
    {
    }

    @Test
    public void testOpt()
    {
        double a = 1.7;
        double x = 20;
        Catenary c = new Catenary(a);
        double y = c.applyAsDouble(x);
        double na = Catenary.aForY(y, x);
        assertEquals(a, na, 1e-10);
    }
    @Test
    public void testInverse()
    {
        double a = 0.5;
        double x = 2;
        Catenary c = new Catenary(a);
        MathFunction inverse = c.inverse();
        assertEquals(x, inverse.applyAsDouble(c.applyAsDouble(x)), 1e-10);
    }
    @Test
    public void testArc()
    {
        double a = 0.5;
        double x = 2;
        Catenary c = new Catenary(a);
        double exp = a*Math.sinh(x/a);
        assertEquals(exp, c.arcLength(0, x), 1e-9);
    }
    @Test
    public void testDerivative()
    {
        double a = 0.5;
        double x = 2;
        Catenary c = new Catenary(a);
        MathFunction derivative = c.derivative();
        assertEquals(27.28991719712775, derivative.applyAsDouble(x), 1e-9);
    }
    @Test
    public void testIntegral()
    {
        double a = 0.5;
        double x = 2;
        Catenary c = new Catenary(a);
        assertEquals(2753.308225072277, c.integral(0, 5), 1e-9);
    }
    @Test
    public void testAForXAndH()
    {
        double x = 25;
        double y = 5;
        double a = Catenary.aForXAndH(x, y);
        Catenary c = new Catenary(a);
        assertEquals(y+a, c.applyAsDouble(x), 1e-9);
    }    
}
