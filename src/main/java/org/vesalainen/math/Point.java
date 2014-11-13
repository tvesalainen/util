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
public class Point implements Serializable
{
    private static final long serialVersionUID = 1L;
    public double x;
    public double y;

    protected static final XComparator xcomp = new XComparator();

    public Point()
    {
    }

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    /**
     * Multiplys p's x and y with k.
     * @param k
     * @param p
     * @return A new Point
     */
    public static final Point mul(double k, Point p)
    {
        return new Point(k*p.x, k*p.y);
    }
    /**
     * Sums points x values to x values and y values to y values
     * @param points
     * @return A new Point
     */
    public static final Point add(Point... points)
    {
        Point res = new Point();
        for (Point pp : points)
        {
            res.x += pp.x;
            res.y += pp.y;
        }
        return res;
    }
    /**
     * Returns a new Point(p1.x - p2.x, p1.y - p2.y)
     * @param p1
     * @param p2
     * @return Returns a new Point(p1.x - p2.x, p1.y - p2.y)
     */
    public static final Point subtract(Point p1, Point p2)
    {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }
    /**
     * Calculates the distance between points
     * @param p1
     * @param p2
     * @return
     */
    public static final double distance(Point p1, Point p2)
    {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    /**
     * @return angle int radians from p1 to p2
     */
    public static final double angle(Point p1, Point p2)
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
     * Creates a new Point distance from p to radians direction
     * @param p Start point
     * @param radians Angle in radians
     * @param distance Distance
     * @return A new Point
     */
    public static final Point move(Point p, double radians, double distance)
    {
        return new Point(
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
    public static final double[] toArray(Point... points)
    {
        double[] res = new double[2*points.length];
        int idx = 0;
        for (Point pp : points)
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
    public static final double maxX(Point... points)
    {
        double max = Double.MIN_VALUE;
        for (Point p : points)
        {
            max = Math.max(max, p.x);
        }
        return max;
    }
    public static final double maxY(Point... points)
    {
        double max = Double.MIN_VALUE;
        for (Point p : points)
        {
            max = Math.max(max, p.y);
        }
        return max;
    }
    public static final double minX(Point... points)
    {
        double min = Double.MAX_VALUE;
        for (Point p : points)
        {
            min = Math.min(min, p.x);
        }
        return min;
    }
    public static final double minY(Point... points)
    {
        double min = Double.MAX_VALUE;
        for (Point p : points)
        {
            min = Math.min(min, p.y);
        }
        return min;
    }
    public static final double maxX(List<Point> points)
    {
        double max = Double.MIN_VALUE;
        for (Point p : points)
        {
            max = Math.max(max, p.x);
        }
        return max;
    }
    public static final double maxY(List<Point> points)
    {
        double max = Double.MIN_VALUE;
        for (Point p : points)
        {
            max = Math.max(max, p.y);
        }
        return max;
    }
    public static final double minX(List<Point> points)
    {
        double min = Double.MAX_VALUE;
        for (Point p : points)
        {
            min = Math.min(min, p.x);
        }
        return min;
    }
    public static final double minY(List<Point> points)
    {
        double min = Double.MAX_VALUE;
        for (Point p : points)
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
     * @param p2 Second Point
     * @return Array of evenly spaced points in a line between p1 and p2
     */
    public static final Point[] midPoints(int count, Point p1, Point p2)
    {
        Point[] res = new Point[count];
        Point gap = Point.subtract(p2, p1);
        Point leg = Point.mul((double)(1)/(double)(count+1), gap);
        for (int ii=0;ii<count;ii++)
        {
            res[ii] = Point.add(p1, Point.mul(ii+1, leg));
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
    public static final int searchX(Point[] points, Point key)
    {
        return Arrays.binarySearch(points, key, xcomp);
    }
    public static final int searchX(List<Point> points, Point key)
    {
        return Collections.binarySearch(points, key, xcomp);
    }
    /**
     * Sorts given array of Points in x-order
     * @param points
     */
    public static final void sortX(Point[] points)
    {
        Arrays.sort(points, xcomp);
    }
    /**
     * Sorts given array of Points in x-order
     * @param points
     */
    public static final void sortX(List<Point> points)
    {
        Collections.sort(points, xcomp);
    }
    /**
     * Compare's to Points x values
     * @param p1
     * @param p2
     * @return
     */
    public static final int compareX(Point p1, Point p2)
    {
        return xcomp.compare(p1, p2);
    }
    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof Point)
        {
            Point pp = (Point) oth;
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
            Point p1 = new Point(0,0);
            Point p2 = new Point(2,2);
            for (int count=1;count<5;count++)
            {
                System.err.println(Arrays.toString(Point.midPoints(count, p1, p2)));
            }
            System.err.println(Math.toDegrees(Point.angle(p1, p2)));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    private static class XComparator implements Comparator<Point>
    {

        public int compare(Point p1, Point p2)
        {
            return Double.compare(p1.x, p2.x);
        }

    }
}
