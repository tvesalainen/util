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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author tkv
 */
public class CurvePoint implements Serializable
{
    private static final long serialVersionUID = 1L;
    public double x;
    public double y;

    protected static final XComparator xcomp = new XComparator();

    public CurvePoint()
    {
    }

    public CurvePoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    /**
     * Multiplys p's x and y with k.
     * @param k
     * @param p
     * @return A new CurvePoint
     */
    public static final CurvePoint mul(double k, CurvePoint p)
    {
        return new CurvePoint(k*p.x, k*p.y);
    }
    /**
     * Sums points x values to x values and y values to y values
     * @param points
     * @return A new CurvePoint
     */
    public static final CurvePoint add(CurvePoint... points)
    {
        CurvePoint res = new CurvePoint();
        for (CurvePoint pp : points)
        {
            res.x += pp.x;
            res.y += pp.y;
        }
        return res;
    }
    /**
     * Returns a new CurvePoint(p1.x - p2.x, p1.y - p2.y)
     * @param p1
     * @param p2
     * @return Returns a new CurvePoint(p1.x - p2.x, p1.y - p2.y)
     */
    public static final CurvePoint subtract(CurvePoint p1, CurvePoint p2)
    {
        return new CurvePoint(p1.x - p2.x, p1.y - p2.y);
    }
    /**
     * Calculates the distance between points
     * @param p1
     * @param p2
     * @return
     */
    public static final double distance(CurvePoint p1, CurvePoint p2)
    {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    /**
     * @return angle int radians from p1 to p2
     */
    public static final double angle(CurvePoint p1, CurvePoint p2)
    {
        double aa = p2.y-p1.y;
        double bb = p2.x-p1.x;
        double dd = Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return dd;
    }
    /**
     * Creates a new CurvePoint distance from p to radians direction
     * @param p Start point
     * @param radians Angle in radians
     * @param distance Distance
     * @return A new CurvePoint
     */
    public static final CurvePoint move(CurvePoint p, double radians, double distance)
    {
        return new CurvePoint(
                p.x + distance*Math.cos(radians),
                p.y + distance*Math.sin(radians)
                );
    }
    /**
     *
     * @return The point as an array [x, y]
     */
    public double[] toArray()
    {
        return new double[] {x, y};
    }
    /**
     * Creates an array for points p1, p2,...
     * @param points
     * @return An array of [p1.x, p1.y, p2.x, p2.y, ...]
     */
    public static final double[] toArray(CurvePoint... points)
    {
        double[] res = new double[2*points.length];
        int idx = 0;
        for (CurvePoint pp : points)
        {
            res[idx++] = pp.x;
            res[idx++] = pp.y;
        }
        return res;
    }
    public void move(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    public static final double maxX(CurvePoint... points)
    {
        double max = Double.MIN_VALUE;
        for (CurvePoint p : points)
        {
            max = Math.max(max, p.x);
        }
        return max;
    }
    public static final double maxY(CurvePoint... points)
    {
        double max = Double.MIN_VALUE;
        for (CurvePoint p : points)
        {
            max = Math.max(max, p.y);
        }
        return max;
    }
    public static final double minX(CurvePoint... points)
    {
        double min = Double.MAX_VALUE;
        for (CurvePoint p : points)
        {
            min = Math.min(min, p.x);
        }
        return min;
    }
    public static final double minY(CurvePoint... points)
    {
        double min = Double.MAX_VALUE;
        for (CurvePoint p : points)
        {
            min = Math.min(min, p.y);
        }
        return min;
    }
    public static final double maxX(List<CurvePoint> points)
    {
        double max = Double.MIN_VALUE;
        for (CurvePoint p : points)
        {
            max = Math.max(max, p.x);
        }
        return max;
    }
    public static final double maxY(List<CurvePoint> points)
    {
        double max = Double.MIN_VALUE;
        for (CurvePoint p : points)
        {
            max = Math.max(max, p.y);
        }
        return max;
    }
    public static final double minX(List<CurvePoint> points)
    {
        double min = Double.MAX_VALUE;
        for (CurvePoint p : points)
        {
            min = Math.min(min, p.x);
        }
        return min;
    }
    public static final double minY(List<CurvePoint> points)
    {
        double min = Double.MAX_VALUE;
        for (CurvePoint p : points)
        {
            min = Math.min(min, p.y);
        }
        return min;
    }
    /**
      * Creates evenly spaced midpoints in a line between p1 and p2
     * Example if count = 1 it creates point in the midle of p1 and p2
     * @param count How many points are created
     * @param p1 First point
     * @param p2 Second CurvePoint
     * @return Array of evenly spaced points in a line between p1 and p2
     */
    public static final CurvePoint[] midPoints(int count, CurvePoint p1, CurvePoint p2)
    {
        CurvePoint[] res = new CurvePoint[count];
        CurvePoint gap = CurvePoint.subtract(p2, p1);
        CurvePoint leg = CurvePoint.mul((double)(1)/(double)(count+1), gap);
        for (int ii=0;ii<count;ii++)
        {
            res[ii] = CurvePoint.add(p1, CurvePoint.mul(ii+1, leg));
        }
        return res;
    }
    /**
     * Searches given key in array in x-order
     * @param points
     * @param key
     * @return Like in Arrays.binarySearch
     * @see java.util.Arrays
     */
    public static final int searchX(CurvePoint[] points, CurvePoint key)
    {
        return Arrays.binarySearch(points, key, xcomp);
    }
    public static final int searchX(List<CurvePoint> points, CurvePoint key)
    {
        return Collections.binarySearch(points, key, xcomp);
    }
    /**
     * Sorts given array of Points in x-order
     * @param points
     */
    public static final void sortX(CurvePoint[] points)
    {
        Arrays.sort(points, xcomp);
    }
    /**
     * Sorts given array of Points in x-order
     * @param points
     */
    public static final void sortX(List<CurvePoint> points)
    {
        Collections.sort(points, xcomp);
    }
    /**
     * Compare's to Points x values
     * @param p1
     * @param p2
     * @return
     */
    public static final int compareX(CurvePoint p1, CurvePoint p2)
    {
        return xcomp.compare(p1, p2);
    }
    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof CurvePoint)
        {
            CurvePoint pp = (CurvePoint) oth;
            return Double.compare(x, pp.x) == 0 && Double.compare(y, pp.y) == 0;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }
    @Override
    public String toString()
    {
        return "P("+x+", "+y+")";
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            CurvePoint p1 = new CurvePoint(0,0);
            CurvePoint p2 = new CurvePoint(2,2);
            for (int count=1;count<5;count++)
            {
                System.err.println(Arrays.toString(CurvePoint.midPoints(count, p1, p2)));
            }
            System.err.println(Math.toDegrees(CurvePoint.angle(p1, p2)));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    private static class XComparator implements Comparator<CurvePoint>
    {

        public int compare(CurvePoint p1, CurvePoint p2)
        {
            return Double.compare(p1.x, p2.x);
        }

    }
}
