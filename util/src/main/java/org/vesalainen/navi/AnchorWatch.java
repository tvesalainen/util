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
import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.Circle;
import org.vesalainen.math.CircleFitter;
import org.vesalainen.math.Circles;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Sector;
import org.vesalainen.math.matrix.DoubleMatrix;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatch extends AbstractLocationObserver implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double DegreeToMeters = 36.0 / 4000000.0;
    private ConvexPolygon area;
    private Polygon externalArea;
    private DoubleMatrix points;
    private DoubleMatrix tempCenter;
    private Point center;
    private AbstractCircle estimated;
    private Circle externalEstimated;
    private ConvexPolygon outer;
    private Polygon externalOuter;
    private CircleFitter fitter;
    private final List<Watcher> watchers = new ArrayList<>();
    private double chainLength = 60 * DegreeToMeters;
    private SafeSector safeSector;
    private SafeSector externalSafeSector;
    private LocalLongitude localLongitude;

    public AnchorWatch()
    {
        reset();
    }

    public final void reset()
    {
        tempCenter = new DoubleMatrix(2, 1);
        points = new DoubleMatrix(0, 2);
        area = new ConvexPolygon();
        outer = new ConvexPolygon();
        center = null;
        estimated = null;
        fitter = null;
        safeSector = null;
        localLongitude = null;
        externalArea = null;
        externalOuter = null;
        externalEstimated = null;
        externalSafeSector = null;
        
    }

    @Override
    public void update(double longitude, double latitude, long time, double accuracy, double speed)
    {
        fireLocation(longitude, latitude, time, accuracy, speed);
        checkLocalLongitude(longitude, latitude);
        double internal = localLongitude.getInternal(longitude);
        doUpdate(internal, latitude, time, accuracy, speed);
    }
    private void doUpdate(double internal, double latitude, long time, double accuracy, double speed)
    {
        if (fitter != null && !safeSector.isInside(internal, latitude))
        {
            double distance = Circles.distanceFromCenter(safeSector, internal, latitude);
            fireAlarm(toMeters(distance));
        }
        if (area.addPoint(internal, latitude))
        {
            fireArea(externalArea);
            points.setReshape(area.points);
            if (fitter == null)
            {
                double radius = CircleFitter.initialCenter(points, tempCenter);
                if (!Double.isNaN(radius))
                {
                    fitter = new CircleFitter();
                    center = new AbstractPoint(tempCenter.get(0, 0), tempCenter.get(0, 1));
                    estimated = new AbstractCircle(center, chainLength);
                    externalEstimated = localLongitude.createExternal(estimated);
                    safeSector = new BasicSafeSector(estimated);
                    externalSafeSector = localLongitude.createExternal(safeSector);
                }
            }
            if (fitter != null)
            {
                if (!area.isInside(center))
                {
                    area.getOuterBoundary(safeSector, outer);
                    fireOuter(externalOuter);
                    fitter.fit(safeSector, outer.points);
                }
                else
                {
                    fitter.fit(safeSector, area.points);
                }
                estimated.set(fitter);
                fireEstimated(externalEstimated);
                fireSafeSector(externalSafeSector);
            }
        }
        else
        {
            double minimumDistance = area.getMinimumDistance(internal, latitude);
            if (!Double.isNaN(accuracy) && !Double.isInfinite(accuracy))
            {
                minimumDistance = Math.max(0, minimumDistance-accuracy);
            }
            fireSuggestNextUpdateIn(minimumDistance/speed, minimumDistance);
        }
    }

    public boolean setAnchorLocation()
    {
        if (safeSector != null && !Double.isNaN(lastLongitude))
        {
            safeSector.set(lastLongitude, lastLatitude);
            return true;
        }
        else
        {
            return false;
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

    public DoubleMatrix getPoints()
    {
        return points;
    }

    public AbstractCircle getEstimated()
    {
        return estimated;
    }

    public ConvexPolygon getOuter()
    {
        return outer;
    }

    public SafeSector getSafeSector()
    {
        return safeSector;
    }

    public void setChainLength(int meters)
    {
        this.chainLength = meters * DegreeToMeters;
    }

    public static double toMeters(double degrees)
    {
        return degrees / DegreeToMeters;
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
    
    private void fireLocation(double x, double y, long time, double accuracy, double speed)
    {
        for (Watcher watcher : watchers)
        {
            watcher.location(x, y, time, accuracy, speed);
        }
    }
    private void fireAlarm(double distance)
    {
        for (Watcher watcher : watchers)
        {
            watcher.alarm(distance);
        }
    }
    private void fireArea(Polygon area)
    {
        for (Watcher watcher : watchers)
        {
            watcher.area(area);
        }
    }
    private void fireOuter(Polygon path)
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
    private void fireSafeSector(SafeSector safe)
    {
        for (Watcher watcher : watchers)
        {
            watcher.safeSector(safe);
        }
    }
    private void fireSuggestNextUpdateIn(double seconds, double meters)
    {
        for (Watcher watcher : watchers)
        {
            watcher.suggestNextUpdateIn(seconds, meters);
        }
    }
    private void checkLocalLongitude(double longitude, double latitude)
    {
        if (localLongitude == null)
        {
            localLongitude = LocalLongitude.getInstance(longitude, latitude);
            externalArea = localLongitude.createExternal(area);
            externalOuter = localLongitude.createExternal(outer);
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
        void location(double x, double y, long time, double accuracy, double speed);
        void area(Polygon area);
        void outer(Polygon path);
        void estimated(Circle estimated);
        void safeSector(SafeSector safe);
        void suggestNextUpdateIn(double seconds, double meters);
    }
    public class Center implements Point
    {
        private double[] data;

        public Center(DoubleMatrix m)
        {
            data = m.data();
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
