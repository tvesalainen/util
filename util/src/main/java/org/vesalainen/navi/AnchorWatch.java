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
import org.vesalainen.math.Circles;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.math.Point;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatch implements Serializable, LocationObserver
{
    private static final long serialVersionUID = 1L;
    private static final double DegreeToMeters = 36.0 / 4000000.0;
    private ConvexPolygon area;
    private DenseMatrix64F points;
    private DenseMatrix64F tempCenter;
    private Point center;
    private AbstractCircle estimated;
    private ConvexPolygon outer;
    private CircleFitter fitter;
    private final List<Watcher> watchers = new ArrayList<>();
    private double chainLength = 60 * DegreeToMeters;
    private SafeSector safeSector;
    private LocalLongitude localLongitude;
    private double lastLongitude = Double.NaN;  // internal
    private double lastLatitude = Double.NaN;
    private long lastTime = -1;

    public AnchorWatch()
    {
        reset();
    }

    public final void reset()
    {
        tempCenter = new DenseMatrix64F(2, 1);
        points = new DenseMatrix64F(0, 2);
        area = new ConvexPolygon();
        outer = new ConvexPolygon();
        center = null;
        estimated = null;
        fitter = null;
        safeSector = null;
        localLongitude = null;
        lastLongitude = Double.NaN;  // internal
        lastLatitude = Double.NaN;
        lastTime = -1;
        
    }

    @Override
    public void update(double longitude, double latitude, long time)
    {
        update(longitude, latitude, time, Double.NaN);
    }
    @Override
    public void update(double longitude, double latitude, long time, double accuracy)
    {
        if (localLongitude == null)
        {
            localLongitude = LocalLongitude.getInstance(longitude, latitude);
        }
        double internal = localLongitude.getInternal(longitude);
        if (Double.isNaN(lastLongitude))
        {
            doUpdate(internal, latitude, time, accuracy, 0);
        }
        else
        {
            double distance = Math.hypot(internal-lastLongitude, latitude-lastLatitude);
            double dTime = (time-lastTime)/1000.0;
            double speed = toMeters(distance/dTime);
            doUpdate(internal, latitude, time, accuracy, speed);
        }
        lastLongitude = internal;
        lastLatitude = latitude;
        lastTime = time;
    }
    @Override
    public void update(double longitude, double latitude, long time, double accuracy, double speed)
    {
        if (localLongitude == null)
        {
            localLongitude = LocalLongitude.getInstance(longitude, latitude);
        }
        double internal = localLongitude.getInternal(longitude);
        doUpdate(internal, latitude, time, accuracy, speed);
        lastLongitude = internal;
        lastLatitude = latitude;
        lastTime = time;
    }
    private void doUpdate(double internal, double latitude, long time, double accuracy, double speed)
    {
        if (fitter != null && !safeSector.isInside(internal, latitude))
        {
            double distance = Circles.distanceFromCenter(safeSector, internal, latitude);
            fireAlarm(toMeters(distance));
        }
        fireLocation(internal, latitude, time, accuracy, speed);
        if (area.addPoint(internal, latitude))
        {
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
                    safeSector = new SafeSector(estimated);
                }
            }
            if (fitter != null)
            {
                if (!area.isInside(center))
                {
                    area.getOuterBoundary(safeSector, outer);
                    fireOuter(outer.points);
                    fitter.fit(safeSector, outer.points);
                }
                else
                {
                    fitter.fit(safeSector, area.points);
                }
                estimated.set(fitter);
                fireEstimated(estimated);
                fireSafeSector(safeSector);
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

    /**
     * Anchor updates.
     * <p>Note! All coordinates are projected for geometry. Y-coordinate is the
     * same as latitude while x-coordinate is cos(latitude) * longitude.
     */
    public interface Watcher
    {
        void alarm(double distance);
        void location(double x, double y, long time, double accuracy, double speed);
        void area(ConvexPolygon area);
        void outer(DenseMatrix64F path);
        void estimated(Circle estimated);
        void safeSector(SafeSector safe);
        void suggestNextUpdateIn(double seconds, double meters);
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
