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

import org.vesalainen.math.matrix.DoubleMatrix;


/**
 *
 * @author Timo Vesalainen
 */
public class ConvexPolygon extends BasicPolygon
{
    private static final long serialVersionUID = 1L;

    public ConvexPolygon()
    {
    }

    public ConvexPolygon(DoubleMatrix points)
    {
        updateConvexPolygon(this, points);
    }

    public void copy(ConvexPolygon oth)
    {
        super.copy(oth);
    }
    
    /**
     * Add new point to convex polygon. Returns true if point was added, false 
     * if point was inside.
     * @param x
     * @param y
     * @return 
     */
    public boolean addPoint(double x, double y)
    {
        return addPoint(points, x, y);
    }
    public static boolean addPoint(DoubleMatrix points, double x, double y)
    {
        if (points.containsRow(x, y))
        {
            return false;
        }
        double[] d = points.data();
        switch (points.rows())
        {
        case 0:
        case 1:
            points.addRow(x, y);
            return true;
        case 2:
            if (Vectors.areAligned(d[0], d[1], d[2], d[3], x, y))
            {
                return addAligned(points, x, y);
            }
            else
            {
                if (Vectors.isClockwise(d[0], d[1], d[2], d[3], x, y))
                {
                    points.insertRow(1, x, y);
                }
                else
                {
                    points.addRow(x, y);
                }
                return true;
            }
        default:
            return add(points, x, y);
        }
    }
    /**
     * Returns minimum distance from inside point (x0, y0) to edge.
     * @param x0
     * @param y0
     * @return 
     */
    public double getMinimumDistance(double x0, double y0)
    {
        double min = Double.MAX_VALUE;
        int rows = points.rows();
        double[] d = points.data();
        double x1 = d[2 * (rows - 1)];
        double y1 = d[2 * (rows - 1) + 1];
        for (int r = 0; r < rows; r++)
        {
            double x2 = d[2 * r];
            double y2 = d[2 * r + 1];
            min=Math.min(min, distanceFromLine(x0, y0, x1, y1, x2, y2));
            x1 = x2;
            y1 = y2;
        }
        if (min > 0 && !isInside(x0, y0))
        {
            throw new IllegalArgumentException("point not inside convex polygon");
        }
        return min;
    }
    /**
     * Returns distance from point (x0, y0) to line that goes through points
     * (x1, y1) and (x2, y2)
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static double distanceFromLine(double x0, double y0, double x1, double y1, double x2, double y2)
    {
        double dx = x2-x1;
        if (dx == 0.0)
        {
            return Math.abs(x1-x0);
        }
        double dy = y2-y1;
        if (dy == 0.0)
        {
            return Math.abs(y1-y0);
        }
        return Math.abs(dy*x0-dx*y0+x2*y1-y2*x1)/Math.hypot(dx, dy);
    }
    public void getOuterBoundary(Point point, ConvexPolygon outer)
    {
        getOuterBoundary(point.getX(), point.getY(), outer);
    }
    public void getOuterBoundary(double x0, double y0, ConvexPolygon outer)
    {
        outer.copy(this);
        outer.addPoint(x0, y0);
        outer.removePoint(x0, y0);
    }
    /**
     * Creates a minimum convex polygon that contains every point either as a 
     * boundary or inside.
     * @param points 2D Points as rows
     * @return 
     */
    public static ConvexPolygon createConvexPolygon(DoubleMatrix points)
    {
        return updateConvexPolygon(new ConvexPolygon(), points);
    }
    /**
     * Creates a minimum convex polygon that contains every point either as a 
     * boundary or inside.
     * <p>Existing points are removed.
     * @param points 2D Points as rows
     */
    public void updateConvexPolygon(DoubleMatrix points)
    {
        updateConvexPolygon(this, points);
    }
    /**
     * Creates a minimum convex polygon that contains every point either as a 
     * boundary or inside.
     * <p>Existing points are removed.
     * @param polygon
     * @param points
     * @return 
     */
    public static ConvexPolygon updateConvexPolygon(ConvexPolygon polygon, DoubleMatrix points)
    {
        assert points.columns() == 2;
        int rows = points.rows();
        double[] d = points.data();
        for (int row=0;row<rows;row++)
        {
            polygon.addPoint(d[2*row], d[2*row+1]);
        }
        return polygon;
    }

    public int getCount()
    {
        return points.rows();
    }
    
    public boolean contains(double x, double y)
    {
        int rows = points.rows();
        double[] d = points.data();
        for (int ii=0;ii<rows;ii++)
        {
            if (d[2*ii] == x && d[2*ii+1] == y)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean addAligned(DoubleMatrix m, double x, double y)
    {
        double[] d = m.data();
        if (d[0] != d[2])
        {
            if (x < d[0] && x < d[2])
            {
                if (d[0] < d[2])
                {
                    d[0] = x;
                    d[1] = y;
                }
                else
                {
                    d[2] = x;
                    d[3] = y;
                }
                return true;
            }
            if (x > d[0] && x > d[2])
            {
                if (d[0] > d[2])
                {
                    d[0] = x;
                    d[1] = y;
                }
                else
                {
                    d[2] = x;
                    d[3] = y;
                }
                return true;
            }
        }
        else
        {
            // vertical
            if (y < d[1] && y < d[3])
            {
                if (d[1] < d[3])
                {
                    d[0] = x;
                    d[1] = y;
                }
                else
                {
                    d[2] = x;
                    d[3] = y;
                }
                return true;
            }
            if (y > d[1] && y > d[3])
            {
                if (d[1] > d[3])
                {
                    d[0] = x;
                    d[1] = y;
                }
                else
                {
                    d[2] = x;
                    d[3] = y;
                }
                return true;
            }
        }
        return false;
    }

    private static boolean add(DoubleMatrix m, double x, double y)
    {
        int len = m.rows();
        double[] d = m.data();
        int ptr=0;
        for (int ii=0;ii<len;ii++)
        {
            int next = (ii+1) % len;
            if (!Vectors.isClockwise(d[2*ii], d[2*ii+1], d[2*next], d[2*next+1], x, y))
            {
                ptr=ii;
                break;
            }
        }
        int start=-1;
        int end=-1;
        for (int cnt=0;cnt<len;cnt++)
        {
            ptr=(ptr+1)%len;
            int next = (ptr+1) % len;
            if (Vectors.isClockwise(d[2*ptr], d[2*ptr+1], d[2*next], d[2*next+1], x, y))
            {
                if (start==-1)
                {
                    start=ptr;
                }
            }
            else
            {
                if (start != -1)
                {
                    end=ptr;
                    break;
                }
            }
        }
        if (start != -1)
        {
            if (((start+1)%len)==end)
            {
                m.insertRow(end, x, y);
            }
            else
            {
                int ii=(start+1)%len;
                int c=0;
                while (ii!=end)
                {
                    d[2*ii]=x;
                    d[2*ii+1]=y;
                    ii=(ii+1)%len;
                    c++;
                }
                if (c>1)
                {
                    m.removeEqualRows();
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private void removePoint(double x0, double y0)
    {
        points.removeRow(x0, y0);
    }

}
