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
package org.vesalainen.ui;

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTransformerTest
{
    
    public DoubleTransformerTest()
    {
    }

    @Test
    public void test1()
    {
        DoubleTransformer triplex = (x,y,n)->{System.err.printf("%f %f\n", x,y);n.accept(3*x, y);};
        DoubleTransformer tripley = (x,y,n)->{System.err.printf("%f %f\n", x,y);n.accept(x, 3*y);};
        DoubleTransformer andThen = triplex.andThen(tripley);
        Point2D.Double exp = new Point2D.Double(3, 3);
        Point2D.Double got = new Point2D.Double();
        andThen.transform(1, 1, (x,y)->{got.setLocation(x, y);System.err.printf("%f %f\n", x,y);});
        assertEquals(exp, got);
    }
    @Test
    public void test2()
    {
        double[] dst = new double[4];
        DoubleTransformer.swap().transform(null, new double[]{1, 2, 3, 4}, dst, 2);
        assertEquals(2, dst[0], 1e-10);
        assertEquals(1, dst[1], 1e-10);
        assertEquals(4, dst[2], 1e-10);
        assertEquals(3, dst[3], 1e-10);
    }    
}
