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

import java.util.ArrayList;
import java.util.List;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.CircleFitter;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.math.Matrices;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatch
{
    private static final double DegreeToMeters = 36.0 / 4000000.0;
    private static final int Size = 50;
    private final ConvexPolygon area = new ConvexPolygon();
    private final DenseMatrix64F points = new DenseMatrix64F(Size, 2);
    private final DenseMatrix64F center = new DenseMatrix64F(2, 1);
    private final DenseMatrix64F outer = new DenseMatrix64F(0, 1);
    private CircleFitter fitter;
    private final List<Watcher> watchers = new ArrayList<>();
    private double chainLength = 60 * DegreeToMeters;

    public AnchorWatch()
    {
        points.reshape(0, 2);
    }

    public void update(double longitude, double latitude)
    {
        longitude *= Math.cos(Math.toRadians(latitude));
        double distance = distance(longitude, latitude);
        if (fitter != null && distance > chainLength)
        {
            fireAlarm(toMeters(distance));
        }
        if (!area.isInside(longitude, latitude))
        {
            fireLocation(longitude, latitude);
            Matrices.addRow(points, longitude, latitude);
            area.updateConvexPolygon(points);
            fireArea(area);
            points.setReshape(area.points);
            if (fitter == null)
            {
                double radius = CircleFitter.initialCenter(points, center);
                if (!Double.isNaN(radius))
                {
                    fireCenter(center.data[0], center.data[1]);
                    fitter = new CircleFitter(center);
                }
            }
            if (fitter != null)
            {
                if (!area.isInside(center.data[0], center.data[1]))
                {
                    area.getOuterBoundary(center, outer);
                    fireOuter(outer);
                    fitter.fit(outer);
                }
                else
                {
                    fitter.fit(area.points);
                }
                fireEstimated(fitter.getX(), fitter.getY(), fitter.getMaxRadius());
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

    public void setChainLength(int meters)
    {
        this.chainLength = meters * DegreeToMeters;
    }

    public static double toMeters(double degrees)
    {
        return degrees / DegreeToMeters;
    }
    
    private double distance(double x, double y)
    {
        double[] d = center.data;
        double cx = d[0];
        double cy = d[1];
        double dx = cx-x;
        double dy = cy-y;
        return Math.hypot(dx, dy);
    }

    public void addWatcher(Watcher watcher)
    {
        watchers.add(watcher);
    }
    
    public void removeWatcher(Watcher watcher)
    {
        watchers.remove(watcher);
    }
    
    private void fireLocation(double x, double y)
    {
        for (Watcher watcher : watchers)
        {
            watcher.location(x, y);
        }
    }
    private void fireAlarm(double distance)
    {
        for (Watcher watcher : watchers)
        {
            watcher.alarm(distance);
        }
    }
    private void fireArea(ConvexPolygon area)
    {
        for (Watcher watcher : watchers)
        {
            watcher.area(area);
        }
    }
    private void fireOuter(DenseMatrix64F path)
    {
        for (Watcher watcher : watchers)
        {
            watcher.outer(path);
        }
    }
    private void fireEstimated(double x, double y, double r)
    {
        for (Watcher watcher : watchers)
        {
            watcher.estimated(x, y, r);
        }
    }
    private void fireCenter(double x, double y)
    {
        for (Watcher watcher : watchers)
        {
            watcher.center(x, y);
        }
    }

    /**
     * Anchor updates.
     * <p>Note! All coordinates are projected for geometry. Y-coordinate is the
     * same as latitude while x-coordinate is cos(latitude) * longitude.
     */
    public interface Watcher
    {
        void alarm(double distance);
        void location(double x, double y);
        void area(ConvexPolygon area);
        void outer(DenseMatrix64F path);
        void center(double x, double y);
        void estimated(double x, double y, double r);
    }
}
