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
package org.vesalainen.math;

import static java.lang.Math.sin;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SineFitterTest
{
    
    @Test
    public void test1()
    {
        SineFitter cf = new SineFitter();
        MathFunction fx = (x)->2*sin(x+1);
        for (int ii=0;ii<6;ii++)
        {
            cf.addPoints(ii, fx.applyAsDouble(ii));
        }
        cf.fit();
        double a = cf.getParamA();
        assertEquals(2, a, 1e-3);
        double b = cf.getParamB();
        assertEquals(1, b, 1e-3);

    }
    
}
