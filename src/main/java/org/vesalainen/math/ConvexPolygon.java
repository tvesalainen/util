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
public class ConvexPolygon extends Polygon
{

    public ConvexPolygon()
    {
    }

    public ConvexPolygon(DenseMatrix64F points)
    {
        super(points);
        if (!isConvex(points))
        {
            throw new IllegalArgumentException("Polygon is not convex");
        }
    }
    public void getOuterBoundary(DenseMatrix64F point, DenseMatrix64F outer)
    {
        getOuterBoundary(point.data[0], point.data[1], outer);
    }
    public void getOuterBoundary(double x0, double y0, DenseMatrix64F outer)
    {
        int rows = points.numRows;
        if (rows < 3 || isInside(x0, y0))
        {
            outer.setReshape(points);
            return;
        }
        double[] d = points.data;
        int left = 0;
        int right = 0;
        double x = d[0];
        double y = d[1];
        double dxLeft  = x-x0;
        double dxRight  = dxLeft;
        double dyLeft = y-y0;
        double dyRight = dyLeft;
        for (int r=1;r<rows;r++)
        {
            x = d[2*r];
            y = d[2*r+1];
            double dx  = x-x0;
            double dy = y-y0;
            if (ConvexPolygon.slopeComp(dx, dy, dxLeft, dyLeft) > 0)
            {
                dxLeft = dx;
                dyLeft = dy;
                left = r;
            }
            if (ConvexPolygon.slopeComp(dx, dy, dxRight, dyRight) < 0)
            {
                dxRight = dx;
                dyRight = dy;
                right = r;
            }
        }
        if (left > right)
        {
            int count = left-right+1;
            outer.reshape(count, 2);
            System.arraycopy(d, 2*right, outer.data, 0, 2*count);
        }
        else
        {
            outer.reshape(rows-(right-left)+1, 2);
            System.arraycopy(d, 2*right, outer.data, 0, 2*(rows-right));
            System.arraycopy(d, 0, outer.data, 2*(rows-right), 2*(left+1));
        }
    }
    /**
     * Creates a minimum convex polygon that contains every point either as a 
     * boundary or inside.
     * @param points 2D Points as rows
     * @return 
     */
    public static ConvexPolygon createConvexPolygon(DenseMatrix64F points)
    {
        return updateConvexPolygon(new ConvexPolygon(), points);
    }
    /**
     * Creates a minimum convex polygon that contains every point either as a 
     * boundary or inside.
     * <p>Existing points are removed.
     * @param points 2D Points as rows
     */
    public void updateConvexPolygon(DenseMatrix64F points)
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
        Matrices.sort(points, new RC(x1, y1, x2, y2, x3, y3, x4, y4));
        Matrices.removeEqualRows(points);
        assert x1 == points.data[0];
        assert y1 == points.data[1];
        DenseMatrix64F m = polygon.points;
        m.reshape(0, 2);
        
        Matrices.addRow(m, x1, y1);
        int left = 1;
        int right = 0;
        int idx = find(points, x2, y2, left);
        if (idx != -1)
        {
            right = idx-1;
            process(
                points, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
            Matrices.addRow(m, x2, y2);
            left = right+2;
        }
        idx = find(points, x3, y3, left);
        if (idx != -1)
        {
            right = idx-1;
            process(
                points, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
            Matrices.addRow(m, x3, y3);
            left = right+2;
        }
        idx = find(points, x4, y4, left);
        if (idx != -1)
        {
            right = idx-1;
            process(
                points, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                points.data[2*(right+1)],
                points.data[2*(right+1)+1],
                m);
            Matrices.addRow(m, x4, y4);
            left = right+2;
        }
        right = points.numRows-1;
        process(
                points, 
                left, 
                right, 
                points.data[2*(left-1)],
                points.data[2*(left-1)+1],
                x1,
                y1,
                m);
        Matrices.removeEqualRows(m);
        polygon.updateBounds();
        assert isConvex(m);
        return polygon;
    }
    private static void process(
            DenseMatrix64F points, 
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
            return;
        }
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
            return;
        }
        double b = yl-m*xl;
        int i = left;
        int j = right;
        while (i <= j && distance(m, b, points.data[2*i], points.data[2*i+1], upper) < 0)
        {
            i++;
        }
        while (i <= j && distance(m, b, points.data[2*j], points.data[2*j+1], upper) < 0)
        {
            j--;
        }
        if (i == j)
        {
            Matrices.addRow(cm, points.data[2*i], points.data[2*i+1]);
        }
        else
        {
            if (i < j)
            {
                int ind = -1;
                double max = Double.NEGATIVE_INFINITY;
                for (int ii=i;ii<=j;ii++)
                {
                    double xx = points.data[2*ii];
                    double yy = points.data[2*ii+1];
                    double mm = distance(m, b, xx, yy, upper);
                    if (mm > max)
                    {
                        max = mm;
                        ind = ii;
                    }
                }
                double nx = points.data[2*ind];
                double ny = points.data[2*ind+1];
                process(points, i, ind-1, xl, yl, nx, ny, cm);
                Matrices.addRow(cm, nx, ny);
                process(points, ind+1, j, nx, ny, xr, yr, cm);
            }
        }
    }
    private static double distance(double m, double b, double x, double y, boolean upper)
    {
        if (upper)
        {
            return y - (m*x+b);
        }
        else
        {
            return (m*x+b) - y;
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
    static boolean isConvex(DenseMatrix64F m)
    {
        int rows = m.numRows-1;
        int cols = m.numCols;
        double[] d = m.data;
        double xf = d[0];
        double yf = d[1];
        double xl = d[cols*rows];
        double yl = d[cols*rows+1];
        double dyr = yf-yl;
        double dxr = xf-xl;
        for (int ii=0;ii<rows;ii++)
        {
            double x1 = d[cols*ii];
            double y1 = d[cols*ii+1];
            double x2 = d[cols*ii+2];
            double y2 = d[cols*ii+3];
            double dyl = y2-y1;
            double dxl = x2-x1;
            if (ConvexPolygon.slopeComp(dxl, dyl, dxr, dyr)<0)
            {
                return false;
            }
            dyr = dyl;
            dxr = dxl;
        }
        return true;
    }
    static int slopeComp(
            double x01,
            double y01,
            double x1,
            double y1,
            double x02,
            double y02,
            double x2,
            double y2
    )
    {
        return ConvexPolygon.slopeComp(
                x1-x01,
                y1-y01,
                x2-x02,
                y2-x02
        );
    }
    /**
     * Return 1 if vector (vx1,vy2) is left of (vx2, vy2). -1 if it is to right.
     * 0 if they equal.
 if same slope.
     * @param vx1
     * @param vy1
     * @param vx2
     * @param vy2
     * @return 
     */
    static int slopeComp(
            double vx1,
            double vy1,
            double vx2,
            double vy2
    )
    {
        if (isInNextSector(vx1, vy1, vx2, vy2))
        {
            return 1;
        }
        else
        {
            if (isInPrevSector(vx1, vy1, vx2, vy2))
            {
                return -1;
            }
            else
            {
                if (isInSameSector(vx1, vy1, vx2, vy2))
                {
                    double m1 = vy1/vx1;
                    double m2 = vy2/vx2;
                    if (m1 > m2)
                    {
                        return 1;
                    }
                    else
                    {
                        if (m1 < m2)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
                else
                {
                    double m1 = vy1/vx1;
                    double m2 = -vy2/-vx2;
                    if (m1 < m2)
                    {
                        return 1;
                    }
                    else
                    {
                        if (m1 > m2)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }
        }
    }
    private static boolean isInOppositeSector(double vx1, double vy1, double vx2, double vy2)
    {
        return isInSameSector(vx1, vy1, -vx2, -vy2);
    }
    /**
     * Returns true is vector(vx1,vy1) and (vx2, vy2) are in same sector.
     * @param vx1
     * @param vy1
     * @param vx2
     * @param vy2
     * @return 
     */
    private static boolean isInSameSector(double vx1, double vy1, double vx2, double vy2)
    {
        if (vx1 >= 0 && vx2 >= 0 && vy1 >= 0 && vy2 >= 0)
        {
            return true;
        }
        if (vx1 <= 0 && vx2 <= 0 && vy1 >= 0 && vy2 >= 0)
        {
            return true;
        }
        if (vx1 <= 0 && vx2 <= 0 && vy1 <= 0 && vy2 <= 0)
        {
            return true;
        }
        if (vx1 >= 0 && vx2 >= 0 && vy1 <= 0 && vy2 <= 0)
        {
            return true;
        }
        return false;
    }
    private static boolean isInPrevSector(double vx1, double vy1, double vx2, double vy2)
    {
        return isInNextSector(vx2, vy2, vx1, vy1);
    }
    private static boolean isInNextSector(double vx1, double vy1, double vx2, double vy2)
    {
        if (vx1 <= 0 && vy1 >= 0 && vx2 >= 0 && vy2 >= 0)
        {
            return true;
        }
        if (vx1 <= 0 && vy1 <= 0 && vx2 <= 0 && vy2 >= 0)
        {
            return true;
        }
        if (vx1 >= 0 && vy1 <= 0 && vx2 <= 0 && vy2 <= 0)
        {
            return true;
        }
        if (vx1 >= 0 && vy1 >= 0 && vx2 >= 0 && vy2 <= 0)
        {
            return true;
        }
        return false;
    }
    private static int sector(double dy, double dx)
    {
        if (dy >= 0)
        {
            if (dx >= 0)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            if (dx <= 0)
            {
                return 2;
            }
            else
            {
                return 3;
            }
        }
    }
    private static class RC implements Matrices.RowComparator
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
            int xys = sec(x, y);
            int ps = sec(px, py);
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

        private int sec(double x, double y)
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
