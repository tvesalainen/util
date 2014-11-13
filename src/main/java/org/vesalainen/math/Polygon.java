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

/**
 * 
 * @author Timo Vesalainen
 */
public class Polygon
{

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
        updateBounds();
    }
    protected void updateBounds()
    {
        bounds.reset();
        int len = points.numRows;
        double[] d = points.data;
        for (int ii=0;ii<len;ii++)
        {
            bounds.update(d[2*ii], d[2*ii+1]);
        }
    }
            
    /**
     * Returns true if point is inside a polygon.
     * @param testx
     * @param testy
     * @return 
     * @see <a href="http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html">PNPOLY - Point Inclusion in Polygon Test W. Randolph Franklin (WRF)</a>
     */
    public boolean isHit(double testx, double testy)
    {
        if (!bounds.isHit(testx, testy))
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
        boolean c = false;
        int nvert = points.numRows;
        int i, j;
        double[] d = points.data;
        for (i = 0, j = nvert - 1; i < nvert; j = i++)
        {
            if (((d[2*i+1] > testy) != (d[2*j+1] > testy))
                    && (testx < (d[2*j] - d[2*i]) * (testy - d[2*i+1]) / (d[2*j+1] - d[2*i+1]) + d[2*i]))
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
    
}
