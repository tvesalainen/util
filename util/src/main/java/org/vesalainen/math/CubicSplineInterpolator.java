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

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import static org.vesalainen.math.BezierCurve.CUBIC;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CubicSplineInterpolator extends AbstractCubicSpline
{
    

    public CubicSplineInterpolator(double... S)
    {
        if (
                S.length % 2 != 0 ||
                S.length < 6
                )
        {
            throw new IllegalArgumentException("wrong number of points");
        }
        this.isInjection = checkInjection(S, 0);
        int n = S.length/2-2;
        Matrix m = get141(n);
        Matrix x = Matrix.getInstance(n, 2);
        for (int ii=0;ii<n;ii++)
        {
            x.setRow(ii, S, 2*(ii+1));
        }
        x.scalarMultiply(6);
        x.subRow(0, S, 0);
        x.subRow(n-1, S, 2*(n+1));
        Matrix B0 = m.solve(x);
        Matrix B = Matrix.getInstance(n+2, 2);
        B.set(1, 0, B0);
        B.setRow(0, S, 0);
        B.setRow(n+1, S, 2*(n+1));
        xArr = new double[n+2];
        controlPoints = new double[6*(n+1)+2];
        int pi = 0;
        controlPoints[pi++] = S[0];
        controlPoints[pi++] = S[1];
        xArr[0] = S[0];
        curves = new ParameterizedOperator[n+1];
        int offset = 0;
        for (int ii=0;ii<curves.length;ii++)
        {
            double bx1 = B.get(ii, 0);
            double by1 = B.get(ii, 1);
            double bx2 = B.get(ii+1, 0);
            double by2 = B.get(ii+1, 1);
            controlPoints[pi++] = bx1+(bx2-bx1)/3;
            controlPoints[pi++] = by1+(by2-by1)/3;
            controlPoints[pi++] = bx1+2*(bx2-bx1)/3;
            controlPoints[pi++] = by1+2*(by2-by1)/3;
            controlPoints[pi++] = S[2*(ii+1)];
            controlPoints[pi++] = S[2*(ii+1)+1];
            curves[ii] = CUBIC.operator(controlPoints, offset);
            offset += 6;
            xArr[ii+1] = S[2*(ii+1)];
        }
    }

    public Shape getShape()
    {
        Path2D p = new Path2D.Double();
        int pi = 0;
        p.moveTo(controlPoints[pi++], controlPoints[pi++]);
        while (pi < controlPoints.length)
        {
            p.curveTo(controlPoints[pi++], controlPoints[pi++], controlPoints[pi++], controlPoints[pi++], controlPoints[pi++], controlPoints[pi++]);
        }
        return p;
    }
    private Matrix get141(int n)
    {
        return Matrix.getInstance(n, n, (i,j)->
        {
            switch (i-j)
            {
                case 0:
                    return 4;
                case 1:
                case -1:
                    return 1;
                default:
                    return 0;
            }
        });
    }
}
