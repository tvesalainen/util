/*
 * Copyright (C) 2011 Timo Vesalainen
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

import java.io.Serializable;


/**
 * @deprecated Use BezierCurve
 * This class implements a Cubic Bezier Curve
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="bb_bezier.pdf">http://www.math.ucla.edu/~baker/149.1.02w/handouts/bb_bezier.pdf</a>
 */
public class CubicBezierCurve0 implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Point[] P;
    private int start;
    /**
     * Creates a CubicBezierCurve by point coordinates
     * @param p Point coordinates x1, y1, x2, y2, x3, y3, x4, y4
     */
    public CubicBezierCurve0(double... p)
    {
        this(makeArr(p));
    }
    /**
     * Creates a CubicBezierCurve
     * @param controlPoints 4 control points starting at 0
     */
    public CubicBezierCurve0(Point... controlPoints)
    {
        if (controlPoints.length < 4)
        {
            throw new IllegalArgumentException("controlPoints length < 4");
        }
        P = controlPoints;
    }
    /**
     * Creates a CubicBezierCurve
     * @param start index
     * @param controlPoints 4 control points starting at start
     */
    public CubicBezierCurve0(int start, Point... controlPoints)
    {
        if (controlPoints.length < start+4)
        {
            throw new IllegalArgumentException("controlPoints length < 4");
        }
        P = controlPoints;
        this.start = start;
    }
    /**
     * Evaluates point in Bezier Curve.
     * @param t Param t in [0,1]
     * @return A CurvePoint in Bezier Curve
     */
    public Point eval(double t)
    {
        return eval(t, new AbstractPoint());
    }
    /**
     * Evaluates point in Bezier Curve. Returned Point is the same as given in 
     * parameter p.
     * @param t
     * @param p
     * @return 
     */
    public Point eval(double t, AbstractPoint p)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t="+t+" not in [0,1]");
        }
        p.set(0, 0);
        double c0 = Math.pow(1-t, 3);
        double c1 = 3*Math.pow(1-t, 2)*t;
        double c2 = 3*(1-t)*t*t;
        double c3 = t*t*t;
        p.add(c0*P[start].getX(), c0*P[start].getY());
        p.add(c1*P[start+1].getX(), c1*P[start+1].getY());
        p.add(c2*P[start+2].getX(), c2*P[start+2].getY());
        p.add(c3*P[start+3].getX(), c3*P[start+3].getY());
        return p;
    }
    private static Point[] makeArr(double... p)
    {
        if (p.length != 8)
        {
            throw new IllegalArgumentException("4 controlPoints need 8 values");
        }
        Point[] cp = new Point[4];
        for (int ii=0;ii<4;ii++)
        {
            cp[ii] = new AbstractPoint(p[2*ii], p[2*ii+1]);
        }
        return cp;
    }
    /**
     * Experimental! makes the start curve like the end
     */
    public void curveStart()
    {
        double d0 = AbstractPoint.angle(P[start], P[start+3]);      // P0 -> P3
        double d1 = AbstractPoint.angle(P[start+3], P[start]);      // P3 -> P0
        double d2 = AbstractPoint.angle(P[start+3], P[start+2]);    // P3 -> P2
        double a1 = d1 - d2;
        double a2 = d0 + a1;
        double di = AbstractPoint.distance(P[start+3], P[start+2]);
        P[start+1] = AbstractPoint.move(P[start], a2, di);
    }
    /**
     * Experimental! makes the end curve like the start
     */
    public void curveEnd()
    {
        double d0 = AbstractPoint.angle(P[start], P[start+3]);      // P0 -> P3
        double d1 = AbstractPoint.angle(P[start+3], P[start]);      // P3 -> P0
        double d2 = AbstractPoint.angle(P[start], P[start+1]);    // P0 -> P1
        double a1 = d2 - d0;
        double a2 = d0 + a1;
        double di = AbstractPoint.distance(P[start], P[start+1]);
        P[start+2] = AbstractPoint.move(P[start+3], a2, di);
    }
}
