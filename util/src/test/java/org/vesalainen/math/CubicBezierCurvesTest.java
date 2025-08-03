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
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.math.BezierCurve.CUBIC;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CubicBezierCurvesTest
{
    
    public CubicBezierCurvesTest()
    {
    }

    @Test
    public void testSplit()
    {
        double t = 0.5;
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        Point2D.Double p2 = new Point2D.Double(-1,-2);
        Point2D.Double p3 = new Point2D.Double(2,1);
        ParameterizedOperator op1 = CUBIC.operator(p0, p1, p2, p3);
        ParameterizedOperator op2 = CubicBezierCurves.firstSplitOperator(t, p0, p1, p2, p3);
        for (double tt=0;tt<t;tt+=0.1)
        {
            assertEquals(op1.calcX(tt), op2.calcX(tt/t), 1e-10);
            assertEquals(op1.calcY(tt), op2.calcY(tt/t), 1e-10);
        }
        ParameterizedOperator op3 = CubicBezierCurves.secondSplitOperator(t, p0, p1, p2, p3);
        for (double tt=t;tt<1;tt+=0.1)
        {
            assertEquals(op1.calcX(tt), op3.calcX((tt-t)/(1-t)), 1e-10);
            assertEquals(op1.calcY(tt), op3.calcY((tt-t)/(1-t)), 1e-10);
        }
    }
    @Test
    public void testMidPoint()
    {
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        assertEquals(p1, CubicBezierCurves.midPoint(1, p0, p1));
    }
}
