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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.Catenary;
import org.vesalainen.math.MathFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CatenaryAnchoringTest
{
    
    public CatenaryAnchoringTest()
    {
    }

    @Test
    public void testDepth()
    {
        double mm = 10;
        CatenaryAnchoring ca = new CatenaryAnchoring(mm);
        double d0 = 5;
        double s0 = 25;
        double Th = ca.horizontalForce(d0, s0);
        for (int ii=1;ii<10;ii++)
        {
            double s1 = ca.chainLengthForHorizontalForce(Th, d0*ii);
            System.err.println(d0*ii+" "+s1);
        }
    }
    @Test
    public void testFairleadTension()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        CatenaryAnchoring ca = new CatenaryAnchoring(mm);
        double F = ca.fairleadTension(s, d);
        assertEquals(s, ca.chainLength(F, d), 1e-10);
    }
    @Test
    public void testHorizontalDistance()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        CatenaryAnchoring ca = new CatenaryAnchoring(mm);
        double F = ca.fairleadTension(s, d);
        double x = ca.horizontalDistance(F, d);
        double a = Catenary.aForXAndH(x, d);
        Catenary c = new Catenary(a);
        assertEquals(d+a, c.applyAsDouble(x), 1e-10);
    }    
    @Test
    public void testHorizontalForce()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        CatenaryAnchoring ca = new CatenaryAnchoring(mm);
        double T = ca.fairleadTension(s, d);
        double Th = ca.horizontalForce(d, s);
        double Tz = ca.w*s;
        assertEquals(T, Math.hypot(Th, Tz), 1e-10);
    }
}
