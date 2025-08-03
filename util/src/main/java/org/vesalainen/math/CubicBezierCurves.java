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
 * CubicBezierCurve helpers.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class CubicBezierCurves
{
    /**
    * Modifies CubicBezierCurve to be x-injection.
    * 
    * <p>It can be proved that Bezier curve is x-injection if it's control points
    * x-components are in ascending order. I.e x0 &le; x1 &le; x2 &le; x3. If Bezier
    * curve's x1 &gt; x2  it can be modified to be x-injection by changing P1 and P2.
     * @param controlPoints
     * @param offset 
     */
    public static boolean forceInjection(double[] controlPoints, int offset)
    {
        double x0 = controlPoints[offset];
        double x1 = controlPoints[offset+2];
        double x2 = controlPoints[offset+4];
        double x3 = controlPoints[offset+6];
        if (x0 > x3)
        {
            throw new IllegalArgumentException("x0 > x3");
        }
        if (!(x0 <= x1 && x1 <= x2 && x2 <= x3))
        {
            double a = (x0+x3)/2.0;
            controlPoints[offset+2] = a;
            controlPoints[offset+4] = a;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static ParameterizedOperator firstSplitOperator(double t, Point2D P0, Point2D P1, Point2D P2, Point2D P3)
    {
        return CUBIC.operator(firstSplit(t, 0, convert(P0, P1, P2, P3)));
    }
    public static ParameterizedOperator secondSplitOperator(double t, Point2D P0, Point2D P1, Point2D P2, Point2D P3)
    {
        return CUBIC.operator(secondSplit(t, 0, convert(P0, P1, P2, P3)));
    }
    public static ParameterizedOperator firstSplitOperator(double t, int offset, double... cp)
    {
        return CUBIC.operator(firstSplit(t, offset, cp));
    }
    public static ParameterizedOperator secondSplitOperator(double t, int offset, double... cp)
    {
        return CUBIC.operator(secondSplit(t, offset, cp));
    }
    public static double[] firstSplit(double t, int offset, double... cp)
    {
        double[] array = Arrays.copyOfRange(cp, offset, offset+8);
        replaceFirstSplit(t, 0, array);
        return array;
    }
    /**
     * Splits curve at t and replaces curve with first divide of split.
     * @param t
     * @param offset
     * @param cp 
     */
    public static void replaceFirstSplit(double t, int offset, double[] cp)
    {
        double Q0x = midPoint(t, cp[offset+0], cp[offset+2]);
        double Q0y = midPoint(t, cp[offset+1], cp[offset+3]);
        double Q1x = midPoint(t, cp[offset+2], cp[offset+4]);
        double Q1y = midPoint(t, cp[offset+3], cp[offset+5]);
        double Q2x = midPoint(t, cp[offset+4], cp[offset+6]);
        double Q2y = midPoint(t, cp[offset+5], cp[offset+7]);
        double R0x = midPoint(t, Q0x, Q1x);
        double R0y = midPoint(t, Q0y, Q1y);
        double R1x = midPoint(t, Q1x, Q2x);
        double R1y = midPoint(t, Q1y, Q2y);
        double S0x = midPoint(t, R0x, R1x);
        double S0y = midPoint(t, R0y, R1y);
        cp[offset+2] = Q0x;
        cp[offset+3] = Q0y;
        cp[offset+4] = R0x;
        cp[offset+5] = R0y;
        cp[offset+6] = S0x;
        cp[offset+7] = S0y;
    }
    public static double[] secondSplit(double t, int offset, double... cp)
    {
        double[] array = Arrays.copyOfRange(cp, offset, offset+8);
        replaceSecondSplit(t, 0, array);
        return array;
    }
    /**
     * Splits curve at t and replaces curve with second divide of split.
     * @param t
     * @param offset
     * @param cp 
     */
    public static void replaceSecondSplit(double t, int offset, double[] cp)
    {
        double Q0x = midPoint(t, cp[offset+0], cp[offset+2]);
        double Q0y = midPoint(t, cp[offset+1], cp[offset+3]);
        double Q1x = midPoint(t, cp[offset+2], cp[offset+4]);
        double Q1y = midPoint(t, cp[offset+3], cp[offset+5]);
        double Q2x = midPoint(t, cp[offset+4], cp[offset+6]);
        double Q2y = midPoint(t, cp[offset+5], cp[offset+7]);
        double R0x = midPoint(t, Q0x, Q1x);
        double R0y = midPoint(t, Q0y, Q1y);
        double R1x = midPoint(t, Q1x, Q2x);
        double R1y = midPoint(t, Q1y, Q2y);
        double S0x = midPoint(t, R0x, R1x);
        double S0y = midPoint(t, R0y, R1y);
        cp[offset+0] = S0x;
        cp[offset+1] = S0y;
        cp[offset+2] = R1x;
        cp[offset+3] = R1y;
        cp[offset+4] = Q2x;
        cp[offset+5] = Q2y;
    }
    public static double[] firstSplit(double t, Point2D P0, Point2D P1, Point2D P2, Point2D P3)
    {
        Point2D Q0 = midPoint(t, P0, P1);
        Point2D Q1 = midPoint(t, P1, P2);
        Point2D Q2 = midPoint(t, P2, P3);
        Point2D R0 = midPoint(t, Q0, Q1);
        Point2D R1 = midPoint(t, Q1, Q2);
        Point2D S0 = midPoint(t, R0, R1);
        return convert(P0, Q0, R0, S0);
    }
    static double[] secondSplit(double t, Point2D P0, Point2D P1, Point2D P2, Point2D P3)
    {
        Point2D Q0 = midPoint(t, P0, P1);
        Point2D Q1 = midPoint(t, P1, P2);
        Point2D Q2 = midPoint(t, P2, P3);
        Point2D R0 = midPoint(t, Q0, Q1);
        Point2D R1 = midPoint(t, Q1, Q2);
        Point2D S0 = midPoint(t, R0, R1);
        return convert(S0, R1, Q2, P3);
    }
    static double midPoint(double t, double p1, double p2)
    {
        return p1+t*(p2-p1);
    }
    static Point2D midPoint(double t, Point2D p1, Point2D p2)
    {
        return new Point2D.Double(p1.getX()+t*(p2.getX()-p1.getX()), p1.getY()+t*(p2.getY()-p1.getY()));
    }
    private static double[] convert(Point2D... points)
    {
        int len = points.length;
        double[] cp = new double[len*2];
        for (int n=0;n<len;n++)
        {
            cp[2*n] = points[n].getX();
            cp[2*n+1] = points[n].getY();
        }
        return cp;
    }

}
