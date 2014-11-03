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

package org.vesalainen.util.math;

import org.ejml.data.DenseMatrix64F;
import org.vesalainen.util.MatrixSort;

/**
 *
 * @author Timo Vesalainen
 */
public class ConvexPolygon extends Polygon
{

    public ConvexPolygon()
    {
    }

    public ConvexPolygon(DenseMatrix64F points)
    {
        super(points);
    }
    
    public static ConvexPolygon createConvexPolygon(DenseMatrix64F points)
    {
        return updateConvexPolygon(new ConvexPolygon(), points);
    }
    public void updateConvexPolygon(DenseMatrix64F points)
    {
        updateConvexPolygon(this, points);
    }
    public static ConvexPolygon updateConvexPolygon(ConvexPolygon polygon, DenseMatrix64F points)
    {
        assert points.numCols == 2;
        double x1 = Double.NaN;
        double y1 = Double.NaN;
        double x2 = Double.NaN;
        double y2 = Double.NaN;
        double x3 = Double.NaN;
        double y3 = Double.NaN;
        double x4 = Double.NaN;
        double y4 = Double.NaN;
        Rect b = new Rect();
        int len = points.numRows;
        double[] d = points.data;
        for (int ii=0;ii<len;ii++)
        {
            b.update(d[2*ii], d[2*ii+1]);
        }
        for (int r=0;r<len;r++)
        {
            double x = d[2*r];
            double y = d[2*r+1];
            if (x == b.xMax)
            {
                x1 = x;
                y1 = y;
            }
            if (y == b.yMax)
            {
                x2 = x;
                y2 = y;
            }
            if (x == b.xMin)
            {
                x3 = x;
                y3 = y;
            }
            if (y == b.yMin)
            {
                x4 = x;
                y4 = y;
            }
        }
        MatrixSort.sort(points, new RC(x1, y1, x2, y2, x3, y3, x4, y4));
        for (int ii=0;ii<len;ii++)
        {
            System.err.println("("+d[2*ii]+", "+d[2*ii+1]+")");
        }
        assert x1 == points.data[0];
        assert y1 == points.data[1];
        DenseMatrix64F m = polygon.points;
        m.reshape(0, 2);
        
        add(m, x1, y1);
        add(m, x2, y2);
        add(m, x3, y3);
        add(m, x4, y4);
        add(m, x1, y1);
        int index = 1;
        int left = 1;
        int right = 0;
        int idx = find(points, x2, y2, left);
        if (idx != -1)
        {
            right = idx-1;
        }
        index += process(
                points, 
                index, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
        
        left = right+2;
        idx = find(points, x3, y3, left);
        if (idx != -1)
        {
            right = idx-1;
        }
        index++;
        index += process(
                points, 
                index, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
        
        left = right+2;
        idx = find(points, x4, y4, left);
        if (idx != -1)
        {
            right = idx-1;
        }
        index++;
        index += process(
                points, 
                index, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);

        left = right+2;
        right = points.numRows-2;
        index++;
        index += process(
                points, 
                index, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
        polygon.updateBounds();
        assert check(polygon.points);
        return polygon;
    }
    private static int process(
            DenseMatrix64F points, 
            int index, 
            int left, 
            int right, 
            double xl,
            double yl,
            double xr,
            double yr,
            DenseMatrix64F cm)
    {
        if (left > right)
        {
            return 0;
        }
        int dIndex = 0;
        double xc;
        double yc;
        int sector;
        if (xl > xr)
        {
            if (yr > yl)
            {
                sector = 1;
                xc = xr;
                yc = yl;
            }
            else
            {
                sector = 2;
                xc = xl;
                yc = yr;
            }
        }
        else
        {
            if (yr < yl)
            {
                sector = 3;
                xc = xr;
                yc = yl;
            }
            else
            {
                sector = 4;
                xc = xl;
                yc = yr;
            }
        }
        boolean upper = sector < 3;
        double m = (yr-yl)/(xr-xl);
        if (Double.isInfinite(m) || Double.isNaN(m))
        {
            return 0;
        }
        double b = yl-m*xl;
        int i = left;
        int j = right;
        while (i <= j && inner(m, b, points.data[2*i], points.data[2*i+1], upper))
        {
            i++;
        }
        while (i <= j && inner(m, b, points.data[2*j], points.data[2*j+1], upper))
        {
            j--;
        }
        if (i == j)
        {
            insert(cm, points.data[2*i], points.data[2*i+1], index);
            dIndex++;
        }
        else
        {
            if (i < j)
            {
                int ind = -1;
                double max = -1;
                for (int ii=i;ii<=j;ii++)
                {
                    double xx = points.data[2*ii];
                    double yy = points.data[2*ii+1];
                    double dx = xx - xc;
                    double dy = yy - yc;
                    double mm = dx*dx+dy*dy;
                    if (mm > max)
                    {
                        max = mm;
                        ind = ii;
                    }
                }
                double nx = points.data[2*ind];
                double ny = points.data[2*ind+1];
                insert(cm, nx, ny, index);
                double di = process(points, index, i, ind-1, xl, yl, nx, ny, cm);
                dIndex += di+1;
                dIndex += process(points, index+dIndex, ind+1, j, nx, ny, xr, yr, cm);
            }
        }
        return dIndex;
    }
    private static boolean inner(double m, double b, double x, double y, boolean upper)
    {
        if (upper)
        {
            return y < m*x+b;
        }
        else
        {
            return y > m*x+b;
        }
    }
    private static int find(DenseMatrix64F m, double x, double y, int start)
    {
        int len = m.numRows;
        for (int ii=start;ii<len;ii++)
        {
            if (x == m.data[2*ii] && y == m.data[2*ii+1])
            {
                return ii;
            }
        }
        return -1;
    }
    private static void add(DenseMatrix64F m, double x, double y)
    {
        int n = m.numRows;
        m.reshape(n+1, 2, true);
        m.data[2*n] = x;
        m.data[2*n+1] = y;
    }
    private static void insert(DenseMatrix64F m, double x, double y, int index)
    {
        int n = m.numRows;
        m.reshape(n+1, 2, true);
        System.arraycopy(m.data, 2*index, m.data, 2*index+2, 2*(n-index));
        m.data[2*index] = x;
        m.data[2*index+1] = y;
    }
    private static boolean check(DenseMatrix64F m)
    {
        int rows = m.numRows-1;
        int cols = m.numCols;
        double[] d = m.data;
        double xf = d[0];
        double yf = d[+1];
        double xl = d[cols*(rows-1)];
        double yl = d[cols*(rows-1)+1];
        double mp = (yf-yl)/(xf-xl);
        System.err.println("slope="+mp);
        for (int ii=0;ii<rows;ii++)
        {
            double x1 = d[cols*ii];
            double y1 = d[cols*ii+1];
            double x2 = d[cols*ii+2];
            double y2 = d[cols*ii+3];
            double mn = (y2-y1)/(x2-x1);
            if (!(
                    (Double.isInfinite(mp) || Double.isNaN(mp) || mp >= 0) && 
                    (Double.isInfinite(mn) || Double.isNaN(mn) || mn <= 0)
                    ))
            {
                if (mn < mp)
                {
                    return false;
                }
            }
            mp = mn;
            System.err.println("slope="+mn);
        }
        return true;
    }

    private static class RC implements MatrixSort.RowComparator
    {
        double x1;
        double y1;
        double x2;
        double y2;
        double x3;
        double y3;
        double x4;
        double y4;

        public RC(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
            this.x4 = x4;
            this.y4 = y4;
        }

        @Override
        public int compare(double[] data, int row, double[] pivot, int len)
        {
            double x = data[len*row];
            double y = data[len*row+1];
            double px = pivot[0];
            double py = pivot[1];
            int xys = sector(x, y);
            int ps = sector(px, py);
            if (xys != ps)
            {
                return xys - ps;
            }
            else
            {
                int res = 0;
                if (x > px)
                {
                    res = -1;
                }
                else
                {
                    if (x < px)
                    {
                        res = 1;
                    }
                }
                if (ps <= 5)
                {
                    return res;
                }
                else
                {
                    return -res;
                }
            }
        }

        private int sector(double x, double y)
        {
            if (x == x1 && y == y1)
            {
                return 1;
            }
            if (x == x2 && y == y2)
            {
                return 3;
            }
            if (x == x3 && y == y3)
            {
                return 5;
            }
            if (x == x4 && y == y4)
            {
                return 7;
            }
            if (x >= x2 && y >= y1)
            {
                return 2;
            }
            if (x <= x2 && y >= y3)
            {
                return 4;
            }
            if (x <= x4 && y <= y3)
            {
                return 6;
            }
            return 8;
        }
    }
}
