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

import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSpline extends RelaxedCubicSpline
{
    private double fullCircle;

    public PolarCubicSpline(double... points)
    {
        this(false, points);
    }
    public PolarCubicSpline(boolean useRadians, double... points)
    {
        super();
        if (useRadians)
        {
            this.fullCircle = Math.PI;
        }
        else
        {
            this.fullCircle = 360;
        }
        int e = 3;
        int pe = points.length;
        double[] pts = new double [points.length+4*e];
        for (int ii=0;ii<e;ii++)
        {
            pts[2*ii] = points[pe-2*e+2*ii]-fullCircle;
            pts[2*ii+1] = points[pe-2*e+1+2*ii];
        }
        System.arraycopy(points, 0, pts, 2*e, pe);
        for (int ii=0;ii<e;ii++)
        {
            pts[2*ii+pts.length-2*e] = points[2*ii]+fullCircle;
            pts[2*ii+pts.length-2*e+1] = points[2*ii+1];
        }
        double[] cp1 = createControlPoints(pts);
        double[] cp2 = Arrays.copyOfRange(cp1, 6*e, cp1.length-6*e);
        init(cp1);
    }
    
}
