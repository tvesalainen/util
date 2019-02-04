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
import org.ejml.data.DenseMatrix64F;

/**
 * 
 * @author Timo Vesalainen
 */
public class Polygon implements Serializable
{
    private static final long serialVersionUID = 1L;

    public final DenseMatrix64F points;
    public final Rect bounds = new Rect();

    public Polygon()
    {
        this(new DenseMatrix64F(0, 2));
    }

    public Polygon(DenseMatrix64F points)
    {
        assert points.numCols == 2;
        this.points = points;
        Matrices.removeEqualRows(points);
        updateBounds();
    }
    protected void copy(Polygon oth)
    {
        points.setReshape(oth.points);
        bounds.set(oth.bounds);
    }
    protected final void updateBounds()
    {
        bounds.reset();
        int len = points.numRows;
        double[] d = points.data;
        for (int ii=0;ii<len;ii++)
        {
            bounds.update(d[2*ii], d[2*ii+1]);
        }
    }
            
    public boolean isInside(Point p)
    {
        return isInside(p.getX(), p.getY());
    }
    /**
     * Returns true if point is inside a polygon.
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in Polygon Test W. Randolph Franklin (WRF)</a>
     */
    public boolean isInside(double testx, double testy)
    {
        if (!bounds.isInside(testx, testy))
        {
            return false;
        }
        return isRawHit(points, testx, testy);
    }
    /**
     * Returns true if point is inside a polygon.
     * <p>Doesn't check the bounding rectangle!
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in Polygon Test W. Randolph Franklin (WRF)</a>
     */
    public static boolean isRawHit(DenseMatrix64F points, double testx, double testy)
    {
        return isRawHit(points.data, points.numRows, testx, testy);
    }
    /**
     * Returns true if point is inside a polygon.
     * <p>Doesn't check the bounding rectangle!
     * @param data x1, y1, x2, y2, ...
     * @param points Number of points (xi, yi)
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in Polygon Test W. Randolph Franklin (WRF)</a>
     */
    public static boolean isRawHit(double[] data, int points, double testx, double testy)
    {
        boolean c = false;
        int i, j;
        for (i = 0, j = points - 1; i < points; j = i++)
        {
            if (((data[2*i+1] > testy) != (data[2*j+1] > testy))
                    && (testx < (data[2*j] - data[2*i]) * (testy - data[2*i+1]) / (data[2*j+1] - data[2*i+1]) + data[2*i]))
            {
                c = !c;
            }
        }
        return c;
    }
    /**
     * Returns true if given point is vertex.
     * <p>This is mainly for testing!
     * @param x
     * @param y
     * @return 
     */
    boolean isVertex(double x, double y)
    {
        int cnt = points.numRows;
        double[] d = points.data;
        for (int ii=0;ii<cnt;ii++)
        {
            if (x == d[2*ii] && y== d[2*ii+1])
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return points.toString();
    }
    /**
     * Returns true if this polygon is convex.
     * @return 
     */
    public boolean isConvex()
    {
        return Polygon.isConvex(points.data, points.numRows);
    }
    /**
     * Returns true if polygon is convex.
     * @param data x1, y1, x2, y2, ...
     * @param points Number of points (xi, yi)
     * @return 
     */
    public static boolean isConvex(double[] data, int points)
    {
        if (points < 3)
        {
            return true;
        }
        for (int i1 = 0; i1 < points; i1++)
        {
            int i2 = (i1 + 1) % points;
            int i3 = (i2 + 1) % points;
            double x1 = data[2 * i1];
            double y1 = data[2 * i1 + 1];
            double x2 = data[2 * i2];
            double y2 = data[2 * i2 + 1];
            double x3 = data[2 * i3];
            double y3 = data[2 * i3 + 1];
            if (Vectors.isClockwise(x1, y1, x2, y2, x3, y3))
            {
                return false;
            }
        }
        return true;
    }
    
}
