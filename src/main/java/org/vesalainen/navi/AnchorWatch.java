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
package org.vesalainen.navi;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.CircleFitter;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatch
{

    private static final double DegreeToMeters = 36.0 / 4000000.0;
    private static final double MaxRadius = 50 * DegreeToMeters;
    private static final double MinRadius = 30 * DegreeToMeters;
    private static final int Size = 10;
    private static final double Limit = 5;  // meters
    private ConvexPolygon area = new ConvexPolygon();
    private final DenseMatrix64F points = new DenseMatrix64F(Size, 2);
    private final DenseMatrix64F center = new DenseMatrix64F(2, 1);
    private final DenseMatrix64F outer = new DenseMatrix64F(0, 1);
    private int index;
    private CircleFitter fitter;
    private final double[] deltaArray = new double[5];
    private int deltaIndex;
    private double delta;
    private double finalCost;
    private DenseMatrix64F meanCenter = new DenseMatrix64F(2, 1);
    private Plotter plotter;
    private int plot;

    public AnchorWatch()
    {
        this(null);
    }

    public AnchorWatch(Plotter plotter)
    {
        this.plotter = plotter;
        Arrays.fill(deltaArray, Double.MAX_VALUE);
    }

    public void update(double longitude, double latitude)
    {
        longitude *= Math.cos(Math.toRadians(latitude));
        if (area.isInside(longitude, latitude))
        {
            return;
        }
        double[] d = points.data;
        d[2 * index] = longitude;
        d[2 * index + 1] = latitude;
        index++;
        if (index == points.numRows)
        {
            double xo = center.data[0];
            double yo = center.data[1];
            area.updateConvexPolygon(points);
            points.reshape(area.points.numRows, 2);
            points.set(area.points);
            if (plotter != null)
            {
                plotter.setColor(Color.BLUE);
                plotter.drawPolygon(area);
            }
            if (fitter == null)
            {
                double radius = CircleFitter.initialCenter(points, center);
                if (!Double.isNaN(radius))
                {
                    fitter = new CircleFitter(center);
                }
            }
            if (fitter != null)
            {
                area.getOuterBoundary(center, outer);
                if (plotter != null)
                {
                    plotter.setColor(Color.ORANGE);
                    //plotter.drawPolygon(outer);
                }
                fitter.fit(outer);
                double dx = xo - center.data[0];
                double dy = yo - center.data[1];
                double dd = Math.sqrt(dx * dx + dy * dy);
                deltaArray[deltaIndex++ % deltaArray.length] = dd;
                delta = 0;
                for (int ii = 0; ii < deltaArray.length; ii++)
                {
                    delta += deltaArray[ii];
                }
                delta /= DegreeToMeters;
                double r = fitter.getRadius() / DegreeToMeters;
                CircleFitter.meanCenter(outer, meanCenter);
                double dxx = center.data[0] - meanCenter.data[0];
                double dyy = center.data[1] - meanCenter.data[1];
                double angle = Math.toDegrees(Math.atan2(dyy, dxx));
                System.err.println(center.data[0] + " " + center.data[1] + " delta=" + delta + " radius=" + r + " angle=" + angle);
                finalCost = fitter.getLevenbergMarquardt().getFinalCost();
            }
            index = points.numRows;
            points.reshape(points.numRows + Size, 2, true);
            if (plotter != null)
            {
                try
                {
                    plotter.plot("test" + plot, "png");
                    plotter.clear();
                    plot++;
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
    }

    public DenseMatrix64F getCenter()
    {
        return center;
    }

    public double getRadius()
    {
        return fitter.getRadius();
    }

    public ConvexPolygon getArea()
    {
        return area;
    }

}
