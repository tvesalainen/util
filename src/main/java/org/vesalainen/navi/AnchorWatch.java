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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.Circle;
import org.vesalainen.math.CircleFitter;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.math.Matrices;
import org.vesalainen.math.Point;
import org.vesalainen.ui.MouldableSector;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatch implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double DegreeToMeters = 36.0 / 4000000.0;
    private ConvexPolygon area;
    private DenseMatrix64F points;
    private DenseMatrix64F tempCenter;
    private Point center;
    private AbstractCircle estimated;
    private DenseMatrix64F outer;
    private CircleFitter fitter;
    private final List<Watcher> watchers = new ArrayList<>();
    private double chainLength = 60 * DegreeToMeters;
    private MouldableSector safeSector;

    public AnchorWatch()
    {
        reset();
    }

    public final void reset()
    {
        tempCenter = new DenseMatrix64F(2, 1);
        points = new DenseMatrix64F(0, 2);
        area = new ConvexPolygon();
        outer = new DenseMatrix64F(0, 1);
        center = null;
        estimated = null;
        fitter = null;
        safeSector = null;
    }

    public void update(double longitude, double latitude)
    {
        longitude *= Math.cos(Math.toRadians(latitude));
        double distance = distance(longitude, latitude);
        if (fitter != null && !safeSector.isInside(longitude, latitude))
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
                double radius = CircleFitter.initialCenter(points, tempCenter);
                if (!Double.isNaN(radius))
                {
                    fitter = new CircleFitter();
                    center = new AbstractPoint(tempCenter.data[0], tempCenter.data[1]);
                    estimated = new AbstractCircle(center, chainLength);
                    safeSector = new MouldableSector(estimated);
                }
            }
            if (fitter != null)
            {
                if (!area.isInside(center))
                {
                    area.getOuterBoundary(tempCenter, outer);
                    fireOuter(outer);
                    fitter.fit(safeSector, outer);
                }
                else
                {
                    fitter.fit(safeSector, area.points);
                }
                estimated.set(fitter);
                fireEstimated(estimated);
                safeSector.update(fitter);
                fireSafeSector(safeSector);
            }
        }
    }

    public Point getCenter()
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
        double[] d = tempCenter.data;
        double cx = d[0];
        double cy = d[1];
        double dx = cx-x;
        double dy = cy-y;
        return Math.hypot(dx, dy);
    }

    public void addWatcher(Watcher watcher)
    {
        if (watcher == null)
        {
            throw new NullPointerException();
        }
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
    private void fireEstimated(Circle estimated)
    {
        for (Watcher watcher : watchers)
        {
            watcher.estimated(estimated);
        }
    }
    private void fireSafeSector(MouldableSector safe)
    {
        for (Watcher watcher : watchers)
        {
            watcher.safeSector(safe);
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
        void estimated(Circle estimated);
        void safeSector(MouldableSector safe);
    }
    public class Center implements Point
    {
        private double[] data;

        public Center(DenseMatrix64F m)
        {
            data = m.data;
        }
        
        @Override
        public double getX()
        {
            return data[0];
        }

        @Override
        public double getY()
        {
            return data[1];
        }
    }
        
}
