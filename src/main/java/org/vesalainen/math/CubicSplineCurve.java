/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * CubicSplineCurve is a utility for interpolating points between known points
 * x0, y0, ... xi, yi, ... xn, yn.
 * <p>Example curve through (0, 1) (1, 3) (2, 2) (3, 4) (4, 3)
 * <p><img src="doc-files/test3.png">
 * @author tkv
 * @see http://www.math.ucla.edu/~baker/149.1.02w/handouts/dd_splines.pdf
 * @see org.vesalainen.math.CubicBezierCurve
 */
public class CubicSplineCurve implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final double Epsilon = 1e-5;
    private double epsilon = Epsilon;
    private Point[] S;
    private Point[] P;
    private CubicBezierCurve[] cbc;
    // temporary
    private AbstractPoint tmpPoint = new AbstractPoint();
    private AbstractPoint tmpKey = new AbstractPoint();
    private CubicSplineCurveKey tmpCSCKey = new CubicSplineCurveKey();
    
    public CubicSplineCurve(List<Point> points)
    {
        this(Epsilon, points.toArray(new Point[points.size()]));
    }
    public CubicSplineCurve(double epsilon, List<Point> points)
    {
        this(epsilon, points.toArray(new Point[points.size()]));
    }
    public CubicSplineCurve(Point... points)
    {
        this(Epsilon, points);
    }
    /**
     * Creates a CubicSplineCurve
     * @param epsilon X coordinate cannot differ more in eval
     * @param points 
     */
    public CubicSplineCurve(double epsilon, Point... points)
    {
        this.epsilon = epsilon;
        S = points;
        Point[] B = new Point[S.length];
        B[0] = S[0];
        B[B.length-1] = S[S.length-1];
        Point[] C = new Point[S.length-2];
        C[0] = AbstractPoint.subtract(AbstractPoint.mul(6, S[1]), S[0]);
        C[C.length-1] = AbstractPoint.subtract(AbstractPoint.mul(6, S[S.length-2]), S[S.length-1]);
        for (int ii=1;ii<S.length-3;ii++)
        {
            C[ii] = AbstractPoint.mul(6, S[ii+1]);
        }
        DenseMatrix64F mC = new DenseMatrix64F(C.length, 2, true, AbstractPoint.toArray(C));
        DenseMatrix64F m141 = get141Matrix(C.length);
        DenseMatrix64F inv = new DenseMatrix64F(m141.numRows, m141.numCols);
        boolean ok = CommonOps.invert(m141, inv);
        if (!ok)
        {
            throw new IllegalArgumentException("cound not invert");
        }
        DenseMatrix64F mB = new DenseMatrix64F(C.length, 2);
        CommonOps.mult(inv, mC, mB);
        for (int ii=1;ii<B.length-1;ii++)
        {
            B[ii] = new AbstractPoint(mB.get(ii-1, 0), mB.get(ii-1, 1));
        }
        P = new Point[3*(B.length-1)+1];
        P[0] = S[0];
        for (int ii=1;ii<B.length;ii++)
        {
            Point[] mids = AbstractPoint.midPoints(2, B[ii-1], B[ii]);
            P[(ii-1)*3+1] = mids[0];
            P[(ii-1)*3+2] = mids[1];
            P[(ii-1)*3+3] = S[ii];
        }
        cbc = new CubicBezierCurve[B.length-1];
        for (int ii=0;ii<cbc.length;ii++)
        {
            cbc[ii] = new CubicBezierCurve(3*ii, P);
        }
     //   cbc[0].curveStart();
     //   cbc[cbc.length-1].curveEnd();
    }

    public Point head()
    {
        return S[0];
    }

    public Point tail()
    {
        return S[S.length-1];
    }

    private Point eval(CubicSplineCurveKey key)
    {
        if (key.t == 0)
        {
            return S[key.bezierIndex];
        }
        return eval(key.bezierIndex, key.t);
    }
    private Point eval(int piece, double t)
    {
        return cbc[piece].eval(t, tmpPoint);
    }
    /**
     * @return Returns the original points.
     */
    public Point[] getPoints()
    {
        return S;
    }
    /**
     * Creates a transposed curve. x <-> y swapped
     * @return
     */
    public CubicSplineCurve transpose()
    {
        List<Point> tr = new ArrayList<Point>();
        for (int ii=0;ii<S.length;ii++)
        {
            tr.add(new AbstractPoint(S[ii].getY(), S[ii].getX()));
        }
        if (tr.get(0).getX() > tr.get(tr.size()-1).getX())    // reverse order
        {
            Collections.reverse(tr);
        }
        return new CubicSplineCurve(tr);
    }
    /**
     * Creates a 1 4 1 matrix eg.
     * |4 1 0|
     * |1 4 1|
     * |0 1 4|
     * @param order Matrix dimension > 1
     * @return 1 4 1 matrix
     */
    public static final DenseMatrix64F get141Matrix(int order)
    {
        if (order < 2)
        {
            throw new IllegalArgumentException("order has to be at least 2 for 1 4 1 matrix");
        }
        double[] data = new double[order*order];
        for (int row=0;row<order;row++)
        {
            for (int col=0;col<order;col++)
            {
                int index = row*order+col;
                if (row == col)
                {
                    data[index] = 4;
                }
                else
                {
                    if (Math.abs(row-col) == 1)
                    {
                        data[index] = 1;
                    }
                    else
                    {
                        data[index] = 0;
                    }
                }
            }
        }
        return new DenseMatrix64F(order, order, true, data);
    }

    public double average(double interval)
    {
        double res = 0;
        Iterator<Point> it = iterator(interval);
        Point p1 = it.next();
        while (it.hasNext())
        {
            Point p2 = it.next();
            res += (p2.getX()-p1.getX())*(p1.getY()+p2.getY())/2;
            p1 = p2;
        }
        return res/(S[S.length-1].getX()-S[0].getX());
    }

    public double average(double startX, double endX, double interval)
    {
        double res = 0;
        Iterator<Point> it = iterator(startX, endX, interval);
        Point p1 = it.next();
        while (it.hasNext())
        {
            Point p2 = it.next();
            res += (p2.getX()-p1.getX())*(p1.getY()+p2.getY())/2;
            p1 = p2;
        }
        return res/(endX-startX);
    }

    public Iterator<Point> iterator(double interval)
    {
        return new Iter(this, interval);
    }

    public Iterator<Point> iterator(double startX, double endX, double interval)
    {
        CubicSplineCurveKey start = getNearestKey(startX, epsilon, new CubicSplineCurveKey());
        CubicSplineCurveKey end = getNearestKey(endX, epsilon, new CubicSplineCurveKey());
        return new Iter(this, interval, start, end);
    }

    private class Iter implements Iterator<Point>
    {
        private CubicSplineCurve csc;
        private int index;
        private int end;
        private double t;
        private double tEnd;
        private double interval;
        private Iter(CubicSplineCurve csc, double interval)
        {
            this.csc = csc;
            end = csc.cbc.length;
            tEnd = 1;
            this.interval = interval;
        }
        private Iter(CubicSplineCurve csc, double interval, CubicSplineCurveKey start, CubicSplineCurveKey end)
        {
            this.csc = csc;
            this.interval = interval;
            this.index = start.bezierIndex;
            this.t = start.t;
            this.end = end.bezierIndex;
            this .tEnd = end.t;
        }
        public boolean hasNext()
        {
            if (index < end)
            {
                return true;
            }
            return index < end && t <= tEnd;
        }

        public Point next()
        {
            Point res = csc.eval(index, t);
            t += interval;
            if (t > 1)
            {
                index++;
                t = 0;
            }
            return res;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Not supported.");
        }

    }
    /**
     * Returns y for x. This is convenient method. Use getNearest to get exact
     * evaluated point.
     * @param x
     * @return 
     * @see org.vesalainen.math.CubicSplineCurve#getNearest(double, double) 
     */
    public double get(double x)
    {
        return get(x, epsilon);
    }
    /**
     * Returns y for x. This is convenient method. Use getNearest to get exact
     * evaluated point.
     * @param x
     * @param epsilon Evaluated points x cannot differ more
     * @return 
     * @see org.vesalainen.math.CubicSplineCurve#getNearest(double, double) 
     */
    public double get(double x, double epsilon)
    {
        Point nearest = getNearest(x, epsilon);
        return nearest.getY();
    }
    /**
     * Searches the nearest Point in curve
     * Note! The curve has to be bijection. I.e. for each x there must be one y
     * 
     * <p>Note! The returned point is only valid till the next call for get or 
     * getNearest.
     * @param x x coordinate
     * @param epsilon Returned Points x-coordinate can not differ more
     * than epsilon from given x
     * @return The nearest point
     * @throws IllegalArgumentException if x is outside range
     */
    public Point getNearest(double x, double epsilon)
    {
        return getNearest(x, epsilon, tmpCSCKey);
    }
    private Point getNearest(double x, double epsilon, CubicSplineCurveKey cscKey)
    {
        CubicSplineCurveKey key = getNearestKey(x, epsilon, cscKey);
        return eval(key);
    }
    private CubicSplineCurveKey getNearestKey(double x, double epsilon, CubicSplineCurveKey key)
    {
        // Eliminate memory rubbish
        if (Math.abs(S[0].getX() - x) < epsilon)
        {
            x = S[0].getX();
        }
        if (Math.abs(S[S.length-1].getX() - x) < epsilon)
        {
            x = S[S.length-1].getX();
        }
        tmpKey.set(x, 0);
        int idx = AbstractPoint.searchX(S, tmpKey);
        if (idx >= 0 && AbstractPoint.compareX(S[idx], tmpKey) == 0)
        {
            key.bezierIndex = idx;
            key.t = 0;
            return key;
        }
        if (idx < 0)
        {
            idx = -idx - 1;
        }
        if (idx == 0 || idx == S.length)
        {
            throw new IllegalArgumentException("x=" + x + " outside interpolator range");
        }
        double t = 0.5;
        double change = 0.25;
        Point res = eval(idx - 1, t);
        while (Math.abs(res.getX() - x) > epsilon)
        {
            if (Double.compare(x, res.getX()) < 0)
            {
                t -= change;
            }
            else
            {
                t += change;
            }
            change /= 2;
            res = eval(idx - 1, t);
        }
        key.bezierIndex = idx-1;
        key.t = t;
        return key;
    }

    public class CubicSplineCurveKey
    {
        protected int bezierIndex;
        protected double t;

        public CubicSplineCurveKey()
        {
        }

        private CubicSplineCurveKey(int bezierIndex, double t)
        {
            this.bezierIndex = bezierIndex;
            this.t = t;
        }
    }
}
