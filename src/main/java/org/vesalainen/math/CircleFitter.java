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

import org.ejml.data.DenseMatrix64F;
import static org.ejml.ops.CommonOps.*;
import org.vesalainen.math.MatrixSort.RowComparator;
import org.vesalainen.math.LevenbergMarquardt.Function;
import org.vesalainen.math.LevenbergMarquardt.JacobianFactory;

/**
 * CircleFitter is a simple class that helps finding circle center and radius for
 * a set of points in the circle. Circle points doesn't have to cover whole circle.
 * A small sector will do.
 * 
 * <p>Implementation is using equations and Jacobian as described in 
 * <a href="https://www.spaceroots.org/documents/circle/circle-fitting.pdf">Finding the circle that best fits a set of points</a>
 * <p>Levenberg-Marquardt algorithm is slightly modified from an example from 
 * <a href="https://code.google.com/p/efficient-java-matrix-library/">EJML</a>
 * @author Timo Vesalainen
 * @see <a href="https://www.spaceroots.org/documents/circle/circle-fitting.pdf">Finding the circle that best fits a set of points</a>
 * @see <a href="http://www.cs.bsu.edu/homepages/kerryj/kjones/circles.pdf">A Few Methods for Fitting Circles to Data</a>
 * @see <a href="https://code.google.com/p/efficient-java-matrix-library/">EJML</a> 
 * @see org.vesalainen.util.math.LevenbergMarquardt
 */
public class CircleFitter implements Function, JacobianFactory
{
    private static final double Epsilon = 1e-10;
    
    private DenseMatrix64F di;
    private DenseMatrix64F center;
    private final DenseMatrix64F zero = new DenseMatrix64F(1, 1);
    private final LevenbergMarquardt levenbergMarquardt = new LevenbergMarquardt(this, this);
    private double radius;
    /**
     * Creates a CircleFitter with estimated center. Estimated center can by 
     * calculated with method initialCenter
     * @param center 
     * @see org.vesalainen.util.math.CircleFitter#initialCenter(org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F) 
     */
    public CircleFitter(DenseMatrix64F center)
    {
        this.center = center;
    }
    /**
     * Fits points to a circle
     * @param points 
     */
    public void fit(DenseMatrix64F points)
    {
        if (zero.numRows != points.numRows)
        {
            zero.reshape(points.numRows, 1);
        }
        if (levenbergMarquardt.optimize(center, points, zero))
        {
            center.set(levenbergMarquardt.getParameters());
        }
        else
        {
            throw new IllegalArgumentException("Fit failed");
        }
    }
    /**
     * Filters points which are closes to the last estimated center.
     * @param points
     * @param center 
     */
    public static void filterInnerPoints(DenseMatrix64F points, DenseMatrix64F center, int minLeft, double percent)
    {
        assert points.numCols == 2;
        assert center.numCols == 1;
        assert center.numRows == 2;
        if (percent <= 0 || percent >= 1)
        {
            throw new IllegalArgumentException("percent "+percent+" is not between 0 & 1");
        }
        DistComp dc = new DistComp(center.data[0], center.data[1]);
        MatrixSort.sort(points, dc);
        int rows = points.numRows;
        double[] d = points.data;
        double limit = dc.distance(d[0], d[1])*percent;
        for (int r=minLeft;r<rows;r++)
        {
            double distance = dc.distance(d[2*r], d[2*r+1]);
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
    public static void limitDistance(DenseMatrix64F p0, DenseMatrix64F pr, double min, double max)
    {
        double x0 = p0.data[0];
        double y0 = p0.data[1];
        double xr = pr.data[0];
        double yr = pr.data[1];
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
                    pr.data[1] = y0 +  r;
                }
                else
                {
                    pr.data[1] = y0 -  r;
                }
            }
            else
            {
                double x = Math.sqrt((r*r)/(m*m+1));
                double y = m*x;
                pr.data[0] = x0 +  x;
                pr.data[1] = y0 +  y;
            }
        }
    }
    /**
     * Calculates mean center of points
     * @param points in
     * @param center out
     * @return 
     */
    public static double meanCenter(DenseMatrix64F points, DenseMatrix64F center)
    {
        assert points.numCols == 2;
        assert center.numCols == 1;
        assert center.numRows == 2;
        center.zero();
        int count = points.numRows;
        double[] d = points.data;
        for (int i=0;i<count;i++)
        {
            center.add(0, 0, d[2*i]);
            center.add(1, 0, d[2*i+1]);
        }
        if (count > 0)
        {
            divide(center, count);
            DenseMatrix64F di = new DenseMatrix64F(points.numRows, 1);
            computeDi(center, points, di);
            return elementSum(di) / (double)points.numRows;
        }
        else
        {
            return Double.NaN;
        }
    }

    /**
     * Calculates an initial estimate for center.
     * @param points
     * @param center
     * @return 
     */
    public static double initialCenter(DenseMatrix64F points, DenseMatrix64F center)
    {
        assert points.numCols == 2;
        assert center.numCols == 1;
        assert center.numRows == 2;
        center.zero();
        int count = 0;
        int len1 = points.numRows;
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
            divide(center, count);
            DenseMatrix64F di = new DenseMatrix64F(points.numRows, 1);
            computeDi(center, points, di);
            return elementSum(di) / (double)points.numRows;
        }
        else
        {
            return Double.NaN;
        }
    }

    private static boolean center(DenseMatrix64F points, int i, int j, int k, DenseMatrix64F center)
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
        
    private void computeDi(DenseMatrix64F center, DenseMatrix64F points)
    {
        if (di == null)
        {
            di = new DenseMatrix64F(points.numRows, 1);
        }
        else
        {
            if (di.numRows != points.numRows)
            {
                di.reshape(points.numRows, 1);
            }
        }
        computeDi(center, points, di);
    }
    private static void computeDi(DenseMatrix64F center, DenseMatrix64F points, DenseMatrix64F di)
    {
        double xx = center.get(0, 0);
        double yy = center.get(1, 0);

        for (int row=0;row<points.numRows;row++)
        {
            double xd = xx - points.get(row, 0);
            double yd = yy - points.get(row, 1);
            double r = Math.sqrt(xd*xd+yd*yd);
            di.set(row, 0, r);
        }
    }
    @Override
    public void compute(DenseMatrix64F center, DenseMatrix64F points, DenseMatrix64F y)
    {
        computeDi(center, points);
        radius = elementSum(di) / (double)points.numRows;
        for (int row=0;row<points.numRows;row++)
        {
            y.data[row] = di.data[row] - radius;
        }
    }

    @Override
    public void computeJacobian(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F jacobian)
    {
        computeDi(param, x);
        double xx = param.get(0, 0);
        double yy = param.get(1, 0);
        double sumXDk = 0;
        double sumYDk = 0;
        int n = x.numRows;
        for (int i=0;i<n;i++)
        {
            sumXDk += (xx - x.get(i, 0))/di.data[i];
        }
        double xDk = sumXDk / n;
        for (int i=0;i<n;i++)
        {
            sumYDk += (yy - x.get(i, 1))/di.data[i];
        }
        double yDk = sumYDk / n;
        for (int i=0;i<n;i++)
        {
            jacobian.set(0, i, (xx - x.get(i, 0))/di.data[i] - xDk);
            jacobian.set(1, i, (yy - x.get(i, 1))/di.data[i] - yDk);
        }
    }

    public DenseMatrix64F getCenter()
    {
        return center;
    }

    public double getRadius()
    {
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
        public int compare(double[] data, int row, double[] pivot, int len)
        {
            double dd = distance(data[row*len], data[row*len+1]);
            double dp = distance(pivot[0], pivot[1]);
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
