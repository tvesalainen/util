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
package org.vesalainen.ui;

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.math.Point;
import org.vesalainen.math.SimplePoint;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChartTransformTest
{
    
    public ChartTransformTest()
    {
    }

    @Test
    public void testInverse()
    {
        Point2D p1 = new Point2D.Double(25, 60);
        Point2D p2 = new Point2D.Double();
        Point2D pt1 = new Point2D.Double();
        Point2D pt2 = new Point2D.Double();
        ChartTransform ct = new ChartTransform();
        ct.transform(p1, pt1);
        DoubleTransform inverse = ct.inverse();
        inverse.transform(pt1, pt2);
        assertEquals(p1, pt2);
    }
    
}
