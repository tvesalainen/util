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
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 * 
 * @author Timo Vesalainen
 */
public class BasicPolygon implements Serializable, Polygon
{
    private static final long serialVersionUID = 1L;

    public final DoubleMatrix points;
    public final Rect bounds = new Rect();

    public BasicPolygon()
    {
        this(new DoubleMatrix(0, 2));
    }

    public BasicPolygon(DoubleMatrix points)
    {
        assert points.columns() == 2;
        this.points = points;
        points.removeEqualRows();
        updateBounds();
    }
    protected void copy(BasicPolygon oth)
    {
        points.setReshape(oth.points);
        bounds.set(oth.bounds);
    }
    protected final void updateBounds()
    {
        bounds.reset();
        int len = points.rows();
        for (int ii=0;ii<len;ii++)
        {
            bounds.update(points.get(ii, 0), points.get(ii, 1));
        }
    }

    @Override
    public void forEach(DoubleBiConsumer op)
    {
        int rows = points.rows();
        for (int ii=0;ii<rows;ii++)
        {
            op.accept(points.get(ii, 0), points.get(ii, 1));
        }
    }

    @Override
    public double getX(int index)
    {
        return points.get(index, 0);
    }

    @Override
    public double getY(int index)
    {
        return points.get(index, 1);
    }

    @Override
    public int count()
    {
        return points.rows();
    }

    @Override
    public Rect bounds()
    {
        return bounds;
    }
    
    @Override
    public boolean isInside(Point p)
    {
        return isInside(p.getX(), p.getY());
    }
    /**
     * Returns true if point is inside a polygon.
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in BasicPolygon Test W. Randolph Franklin (WRF)</a>
     */
    @Override
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
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in BasicPolygon Test W. Randolph Franklin (WRF)</a>
     */
    public static boolean isRawHit(DoubleMatrix points, double testx, double testy)
    {
        return isRawHit(points.data(), points.rows(), testx, testy);
    }
    /**
     * Returns true if point is inside a polygon.
     * <p>Doesn't check the bounding rectangle!
     * @param data x1, y1, x2, y2, ...
     * @param points Number of points (xi, yi)
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in BasicPolygon Test W. Randolph Franklin (WRF)</a>
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
        int cnt = points.rows();
        double[] d = points.data();
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
        return BasicPolygon.isConvex(points.data(), points.rows());
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
