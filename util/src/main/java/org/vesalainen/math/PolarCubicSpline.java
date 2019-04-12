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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSpline extends RelaxedCubicSpline
{

    public PolarCubicSpline(double... points)
    {
        this(false, 3, points);
    }
    public PolarCubicSpline(boolean useRadians, int external, double... points)
    {
        super(createControlPoints(useRadians, external, points));
    }
    private static double[] createControlPoints(boolean useRadians, int external, double... points)
    {
        double fullCircle;
        if (useRadians)
        {
            fullCircle = 2*Math.PI;
        }
        else
        {
            fullCircle = 360;
        }
        int pe = points.length;
        double[] pts = new double [points.length+4*external];
        for (int ii=0;ii<external;ii++)
        {
            pts[2*ii] = points[pe-2*external+2*ii]-fullCircle;
            pts[2*ii+1] = points[pe-2*external+1+2*ii];
        }
        System.arraycopy(points, 0, pts, 2*external, pe);
        for (int ii=0;ii<external;ii++)
        {
            pts[2*ii+pts.length-2*external] = points[2*ii]+fullCircle;
            pts[2*ii+pts.length-2*external+1] = points[2*ii+1];
        }
        return pts;
    }
    
}
