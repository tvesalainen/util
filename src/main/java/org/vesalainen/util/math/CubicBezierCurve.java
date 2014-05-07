/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.math;

import org.vesalainen.math.Point;
import java.io.Serializable;


/**
 * This class implements a Cubic Bezier Curve
 * @author tkv
 * @see http://www.math.ucla.edu/~baker/149.1.02w/handouts/bb_bezier.pdf
 */
public class CubicBezierCurve implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Point[] P;
    private int start;

    protected CubicBezierCurve()
    {
    }
    /**
     * Creates a CubicBezierCurve
     * @param controlPoints 4 control points starting at 0
     */
    public CubicBezierCurve(Point... controlPoints)
    {
        P = controlPoints;
    }
    /**
     * Creates a CubicBezierCurve
     * @param controlPoints 4 control points starting at start
     * @param Start index
     */
    public CubicBezierCurve(int start, Point... controlPoints)
    {
        P = controlPoints;
        this.start = start;
    }
    /**
     * Evaluates points in Bezier Curve
     * @param t Param t in [0,1]
     * @return A Point in Bezier Curve
     */
    public Point eval(double t)
    {
        if (t < 0 || t > 1)
        {
            throw new IllegalArgumentException("t="+t+" not in [0,1]");
        }
        return Point.add(
                Point.mul(Math.pow(1-t, 3), P[start]),
                Point.mul(3*Math.pow(1-t, 2)*t, P[start+1]),
                Point.mul(3*(1-t)*t*t, P[start+2]),
                Point.mul(t*t*t, P[start+3])
                );
    }
    /**
     * Experimental! makes the start curve like the end
     */
    public void curveStart()
    {
        double d0 = Point.angle(P[start], P[start+3]);      // P0 -> P3
        double d1 = Point.angle(P[start+3], P[start]);      // P3 -> P0
        double d2 = Point.angle(P[start+3], P[start+2]);    // P3 -> P2
        double a1 = d1 - d2;
        double a2 = d0 + a1;
        double di = Point.distance(P[start+3], P[start+2]);
        P[start+1] = Point.move(P[start], a2, di);
    }
    /**
     * Experimental! makes the end curve like the start
     */
    public void curveEnd()
    {
        double d0 = Point.angle(P[start], P[start+3]);      // P0 -> P3
        double d1 = Point.angle(P[start+3], P[start]);      // P3 -> P0
        double d2 = Point.angle(P[start], P[start+1]);    // P0 -> P1
        double a1 = d2 - d0;
        double a2 = d0 + a1;
        double di = Point.distance(P[start], P[start+1]);
        P[start+2] = Point.move(P[start+3], a2, di);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
           CubicBezierCurve cbc = new CubicBezierCurve(
                   new Point(2,3),
                   new Point(0,5),
                   new Point(-1,-2),
                   new Point(2,1)
                   );
           for (double t = 0;t<1;t += 0.1)
           {
            System.err.println(cbc.eval(t));
           }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
