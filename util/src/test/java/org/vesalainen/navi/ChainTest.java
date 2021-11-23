/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
public class ChainTest
{
    
    public ChainTest()
    {
    }

    @Test
    public void testCatenary()
    {
        double s = 40;
        double d = 10;
        double mm = 10;
        double w = Chain.chainWeight(mm);
        double g = 9.80665;
        Chain ch = new Chain(mm);
        double T = ch.fairleadMinimumTensionForChain(s, d);
        double a = T/(w);
        Catenary cat = new Catenary(a, -a);
        MathFunction inv = cat.inverse();
        double x1 = inv.applyAsDouble(d);
        double y = cat.applyAsDouble(x1);
        double arc = cat.arcLength().applyAsDouble(x1);
        double x2 = ch.horizontalScope(T, d);
        assertEquals(x1, x2, 1e-10);
    }
    
}
