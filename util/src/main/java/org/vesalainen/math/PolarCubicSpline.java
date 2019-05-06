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
import java.util.Arrays;
import static org.vesalainen.math.BezierCurve.CUBIC;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSpline extends RelaxedCubicSpline
{
    private static final double FULL_CIRCLE = 360;

    public PolarCubicSpline(Point2D... points)
    {
        this(convert(points));
    }
    
    public PolarCubicSpline(double... points)
    {
        this(Math.min(4, points.length/2), points);
    }
    public PolarCubicSpline(int external, double... points)
    {
        super(createPoints(external, points));
    }
    private static double[] createPoints(int external, double... points)
    {
        if (external > points.length/2)
        {
            throw new IllegalArgumentException("not enough points for external");
        }
        int pe = points.length;
        double[] pts = new double [points.length+4*external];
        for (int ii=0;ii<external;ii++)
        {
            pts[2*ii] = points[pe-2*external+2*ii]-FULL_CIRCLE;
            pts[2*ii+1] = points[pe-2*external+1+2*ii];
        }
        System.arraycopy(points, 0, pts, 2*external, pe);
        for (int ii=0;ii<external;ii++)
        {
            pts[2*ii+pts.length-2*external] = points[2*ii]+FULL_CIRCLE;
            pts[2*ii+pts.length-2*external+1] = points[2*ii+1];
        }
        return pts;
    }

    @Override
    protected double[] createControlPoints(double[] points)
    {
        double[] cp = super.createControlPoints(points);
        int o1=0;
        for (;;o1+=6)
        {
            if (cp[o1] <= 0 && cp[o1+6] > 0)
            {
                break;
            }
        }
        CubicBezierCurves.forceInjection(cp, o1);
        ParameterizedOperator op1 = CUBIC.operator(cp, o1);
        double t1 = op1.evalTForX(0, 0.01);
        CubicBezierCurves.replaceSecondSplit(t1, o1, cp);
        int o2 = cp.length - 8;
        for (;;o2-=6)
        {
            if (cp[o2] < FULL_CIRCLE && cp[o2+6] >= FULL_CIRCLE)
            {
                break;
            }
        }
        CubicBezierCurves.forceInjection(cp, o2);
        ParameterizedOperator op2 = CUBIC.operator(cp, o2);
        double t2 = op2.evalTForX(FULL_CIRCLE, 0.01);
        CubicBezierCurves.replaceFirstSplit(t2, o2, cp);
        return Arrays.copyOfRange(cp, o1, o2+8);
    }

}
