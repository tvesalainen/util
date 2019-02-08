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

import java.awt.geom.Point2D;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BezierCurveTest
{
    
    public BezierCurveTest()
    {
    }

    @Test
    public void test1()
    {
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        Point2D.Double p2 = new Point2D.Double(-1,-2);
        Point2D.Double p3 = new Point2D.Double(2,1);
        BezierCurve bc = new BezierCurve(3);
        assertEquals(p0, bc.calc(0, p0, p1, p2, p3));
        assertEquals(p3, bc.calc(1, p0, p1, p2, p3));
        assertTrue(bc.pathLength(0.1, p0, p1, p2, p3) < bc.pathLengthEstimate(p0, p1, p2, p3));
    }
    @Test
    public void test2()
    {
        Random r = new Random(123456);
        BezierCurve bc = new BezierCurve(3);
        for (int ii=0;ii<1000;ii++)
        {
            double[] cp = new double[8];
            for (int jj=0;jj<cp.length;jj++)
            {
                cp[jj] = r.nextDouble();
            }
            double pathLength = bc.pathLength(0.1, cp);
            double pathLengthEstimate = bc.pathLengthEstimate(cp);
            System.err.println(pathLength+" < "+pathLengthEstimate);
            assertTrue(pathLength < pathLengthEstimate);
        }
    }
    
}
