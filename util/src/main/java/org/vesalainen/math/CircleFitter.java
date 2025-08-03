/*
 * Copyright (C) 2014 Timo Vesalainen
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
import java.lang.reflect.Array;
import org.vesalainen.math.LevenbergMarquardt.Function;
import org.vesalainen.math.LevenbergMarquardt.JacobianFactory;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;
import org.vesalainen.util.ArrayHelp.RowComparator;

/**
 * CircleFitter is a simple class that helps finding circle tempCenter and radius for
 a set of points in the circle. Circle points doesn't have to cover whole circle.
 * A small sector will do.
 * 
 * <p>Implementation is using equations and Jacobian as described in 
 * <a href="https://www.spaceroots.org/documents/circle/circle-fitting.pdf">Finding the circle that best fits a set of points</a>
 * <p>Levenberg-Marquardt algorithm is slightly modified from an example from 
 * @author Timo Vesalainen
 * @see <a href="https://www.spaceroots.org/documents/circle/circle-fitting.pdf">Finding the circle that best fits a set of points</a>
 * @see <a href="http://www.cs.bsu.edu/homepages/kerryj/kjones/circles.pdf">A Few Methods for Fitting Circles to Data</a>
 * @see org.vesalainen.math.LevenbergMarquardt
 */
public class CircleFitter implements Function, JacobianFactory, Circle, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double Epsilon = 1e-10;
    
    private DoubleMatrix di;
    private final DoubleMatrix initCenter = new DoubleMatrix(2, 1);
    private final DoubleMatrix zero = new DoubleMatrix(1, 1);
    private final LevenbergMarquardt levenbergMarquardt = new LevenbergMarquardt(this, this);
    private double radius;
    private DoubleMatrix points;
    /**
     * Creates a CircleFitter with estimated tempCenter. Estimated tempCenter can by 
 calculated with method initialCenter
     * @see org.vesalainen.math.CircleFitter#initialCenter(org.ejml.data.DoubleMatrix, org.ejml.data.DoubleMatrix) 
     */
    public CircleFitter()
    {
    }
    /**
     * Fits points to a circle
     * @param points 
     */
    public Circle fit(Point initPoint, DoubleMatrix points)
    {
        initCenter.set(0, 0, initPoint.getX());
        initCenter.set(1, 0, initPoint.getY());
        radius = Double.NaN;
        fit(points);
        return this;
    }
    public Circle fit(DoubleMatrix initPoint, DoubleMatrix points)
    {
        initCenter.setReshape(initPoint);
        radius = Double.NaN;
        fit(points);
        return this;
    }
    /**
     * Fits points to a circle
     * @param points 
     */
    public Circle fit(Circle initCircle, DoubleMatrix points)
    {
        initCenter.set(0, 0, initCircle.getX());
        initCenter.set(1, 0, initCircle.getY());
        radius = initCircle.getRadius();
        fit(points);
        return this;
    }
    private void fit(DoubleMatrix points)
    {
        this.points = points;
        radius = Double.NaN;
        if (zero.rows() != points.rows())
        {
            zero.reshape(points.rows(), 1);
        }
        if (levenbergMarquardt.optimize(initCenter, points, zero))
        {
            initCenter.set(levenbergMarquardt.getParameters());
        }
        else
        {
            throw new IllegalArgumentException("Fit failed");
        }
    }
    /**
     * Filters points which are closes to the last estimated tempCenter.
     * @param points
     * @param center 
     */
    public static void filterInnerPoints(DoubleMatrix points, DoubleMatrix center, int minLeft, double percent)
    {
        assert points.columns() == 2;
        assert center.columns() == 1;
        assert center.rows() == 2;
        if (percent <= 0 || percent >= 1)
        {
            throw new IllegalArgumentException("percent "+percent+" is not between 0 & 1");
        }
        DistComp dc = new DistComp(center.get(0, 0), center.get(1, 0));
        points.sort(dc);
        int rows = points.rows();
        double limit = dc.distance(points.get(0, 0), points.get(0, 1))*percent;
        for (int r=minLeft;r<rows;r++)
        {
            double distance = dc.distance(points.get(r, 0), points.get(r, 1));
            if (distance < limit)
            {
                points.reshape(r/2, 2, true);
                break;
            }
        }
    }
    /**
     * Changes pr so that distance from p0 is in range min - max. Slope of (p0,pr)
     * remains the same.
     * @param p0
     * @param pr
     * @param min
     * @param max 
     */
    public static void limitDistance(DoubleMatrix p0, DoubleMatrix pr, double min, double max)
    {
        double x0 = p0.get(0, 0);
        double y0 = p0.get(1, 0);
        double xr = pr.get(0, 0);;
        double yr = pr.get(1, 0);;
        double dx = xr-x0;
        double dy = yr-y0;
        double r = Math.sqrt(dx*dx+dy*dy);
        if (r < min || r > max)
        {
            if (r < min)
            {
                r = min;
            }
            else
            {
                r = max;
            }
            double m = dy/dx;
            if (Double.isInfinite(m))
            {
                if (m > 0)
                {
                    pr.set(0, 1, y0 +  r);
                }
                else
                {
                    pr.set(0, 1, y0 -  r);
                }
            }
            else
            {
                double x = Math.sqrt((r*r)/(m*m+1));
                double y = m*x;
                pr.set(0, 0, x0 +  x);
                pr.set(0, 1, y0 +  y);
            }
        }
    }
    /**
     * Calculates mean tempCenter of points
     * @param points in
     * @param center out
     * @return 
     */
    public static double meanCenter(DoubleMatrix points, DoubleMatrix center)
    {
        assert points.columns() == 2;
        assert center.columns() == 1;
        assert center.rows() == 2;
        center.zero();
        int count = points.rows();
        for (int i=0;i<count;i++)
        {
            center.add(0, 0, points.get(i, 0));
            center.add(1, 0, points.get(i, 1));
        }
        if (count > 0)
        {
            DoubleMatrix.divide(center, count);
            DoubleMatrix di = new DoubleMatrix(points.rows(), 1);
            computeDi(center, points, di);
            return DoubleMatrix.elementSum(di) / (double)points.rows();
        }
        else
        {
            return Double.NaN;
        }
    }

    /**
     * Calculates an initial estimate for tempCenter.
     * @param points
     * @param center
     * @return 
     */
    public static double initialCenter(DoubleMatrix points, DoubleMatrix center)
    {
        assert points.columns() == 2;
        assert center.columns() == 1;
        assert center.rows() == 2;
        center.zero();
        int count = 0;
        int len1 = points.rows();
        int len2 = len1-1;
        int len3 = len2-1;
        for (int i=0;i<len3;i++)
        {
            for (int j=i+1;j<len2;j++)
            {
                for (int k=j+1;k<len1;k++)
                {
                    if (center(points, i, j, k, center))
                    {
                        count++;
                    }
                }
            }
        }
        if (count > 0)
        {
            DoubleMatrix.divide(center, count);
            DoubleMatrix di = new DoubleMatrix(points.rows(), 1);
            computeDi(center, points, di);
            return DoubleMatrix.elementSum(di) / (double)points.rows();
        }
        else
        {
            return Double.NaN;
        }
    }

    private static boolean center(DoubleMatrix points, int i, int j, int k, DoubleMatrix center)
    {
        double xi = points.get(i, 0);
        double yi = points.get(i, 1);
        double xj = points.get(j, 0);
        double yj = points.get(j, 1);
        double xk = points.get(k, 0);
        double yk = points.get(k, 1);
        double det = (xk-xj)*(yj-yi)-(xj-xi)*(yk-yj);
        if (Math.abs(det) < Epsilon)
        {
            return false;   // aligned
        }
        double det2 = 2.0*det;
        double xyi2 = xi*xi+yi*yi;
        double xyj2 = xj*xj+yj*yj;
        double xyk2 = xk*xk+yk*yk;
        double x = ((yk-yj)*xyi2+(yi-yk)*xyj2+(yj-yi)*xyk2)/det2;
        double y = -((xk-xj)*xyi2+(xi-xk)*xyj2+(xj-xi)*xyk2)/det2;
        center.add(0, 0, x);
        center.add(1, 0, y);
        return true;
    }
        
    private void computeDi(DoubleMatrix center, ReadableDoubleMatrix points)
    {
        if (di == null)
        {
            di = new DoubleMatrix(points.rows(), 1);
        }
        else
        {
            if (di.rows() != points.rows())
            {
                di.reshape(points.rows(), 1);
            }
        }
        computeDi(center, points, di);
    }
    private static void computeDi(DoubleMatrix center, ReadableDoubleMatrix points, DoubleMatrix di)
    {
        double xx = center.get(0, 0);
        double yy = center.get(1, 0);

        for (int row=0;row<points.rows();row++)
        {
            double xd = xx - points.get(row, 0);
            double yd = yy - points.get(row, 1);
            double r = Math.hypot(xd, yd);
            di.set(row, 0, r);
        }
    }
    @Override
    public void compute(DoubleMatrix center, ReadableDoubleMatrix points, DoubleMatrix y)
    {
        double r;
        if (Double.isNaN(radius))
        {
            computeDi(center, points);
            r = DoubleMatrix.elementSum(di) / (double)points.rows();
        }
        else
        {
            r = radius;
        }
        int len = points.rows();
        for (int row=0;row<len;row++)
        {
            y.set(row, 0, di.get(row, 0) - r);
        }
    }

    @Override
    public void computeJacobian(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix jacobian)
    {
        computeDi(param, x);
        double xx = param.get(0, 0);
        double yy = param.get(1, 0);
        double sumXDk = 0;
        double sumYDk = 0;
        int n = x.rows();
        for (int i=0;i<n;i++)
        {
            sumXDk += (xx - x.get(i, 0))/di.get(i, 0);
        }
        double xDk = sumXDk / n;
        for (int i=0;i<n;i++)
        {
            sumYDk += (yy - x.get(i, 1))/di.get(i, 0);
        }
        double yDk = sumYDk / n;
        for (int i=0;i<n;i++)
        {
            jacobian.set(0, i, (xx - x.get(i, 0))/di.get(i, 0) - xDk);
            jacobian.set(1, i, (yy - x.get(i, 1))/di.get(i, 0) - yDk);
        }
    }

    @Override
    public double getX()
    {
        return initCenter.get(0, 0);
    }

    @Override
    public double getY()
    {
        return initCenter.get(1, 0);
    }

    @Override
    public double getRadius()
    {
        if (Double.isNaN(radius))
        {
            computeDi(initCenter, points);
            radius = DoubleMatrix.elementSum(di) / (double)points.rows();
        }
        return radius;
    }

    public LevenbergMarquardt getLevenbergMarquardt()
    {
        return levenbergMarquardt;
    }

    private static class DistComp implements RowComparator
    {
        private final double x;
        private final double y;

        public DistComp(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public int compare(Object data, int row, Object pivot, int len)
        {
            double dd = distance(Array.getDouble(data, row*len), Array.getDouble(data, row*len+1));
            double dp = distance(Array.getDouble(pivot, 0), Array.getDouble(pivot, 1));
            if (dd < dp)
            {
                return 1;
            }
            else
            {
                if (dd > dp)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        }
        private double distance(double xx, double yy)
        {
            double dx = x-xx;
            double dy = y-yy;
            return Math.sqrt(dx*dx+dy*dy);
        }
    }
}
