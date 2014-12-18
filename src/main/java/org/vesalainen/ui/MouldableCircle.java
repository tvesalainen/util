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

package org.vesalainen.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Circles;
import org.vesalainen.math.Point;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableCircle implements Circle, Serializable
{
    private static final long serialVersionUID = 1L;
    protected AbstractCircle circle;
    protected boolean attached = true;
    protected AbstractPoint attachPoint;
    protected List<MouldableCircleObserver> observers = new ArrayList<>();

    public MouldableCircle(AbstractCircle circle)
    {
        this.circle = circle;
        attachPoint = new AbstractPoint(circle);
    }

    public void update(Point center)
    {
        update(center.getX(), center.getY());
    }
    
    public void update(double x, double y)
    {
        if (attached)
        {
            circle.setX(x);
            circle.setY(y);
            fireCenter(x, y);
        }
        attachPoint.setX(x);
        attachPoint.setY(y);
    }

    public void set(double x, double y)
    {
        attached = false;
        attachPoint.setX(x);
        attachPoint.setY(y);
        fireCenter(x, y);
    }

    @Override
    public double getX()
    {
        if (attached)
        {
            return circle.getX();
        }
        else
        {
            return attachPoint.getX();
        }
    }

    public void setX(double x)
    {
        attached = false;
        attachPoint.setX(x);
        fireCenter(x, getY());
    }

    @Override
    public double getY()
    {
        if (attached)
        {
            return circle.getY();
        }
        else
        {
            return attachPoint.getY();
        }
    }

    public void setY(double y)
    {
        attached = false;
        attachPoint.setY(y);
        fireCenter(getX(), y);
    }

    @Override
    public double getRadius()
    {
        return circle.getRadius();
    }

    public void setRadius(double radius)
    {
        circle.setRadius(radius);
    }

    public boolean isInside(double x, double y)
    {
        return circle.isInside(x, y);
    }

    public boolean isInSector(double x, double y)
    {
        return true;
    }
    
    public void addObserver(MouldableCircleObserver observer)
    {
        observers.add(observer);
    }
    
    public void removeObserver(MouldableCircleObserver observer)
    {
        observers.remove(observer);
    }
    
    protected void fireCenter(double x, double y)
    {
        for (MouldableCircleObserver observer : observers)
        {
            observer.centerMoved(x, y);
        }
    }

    protected void fireRadius(double radius)
    {
        for (MouldableCircleObserver observer : observers)
        {
            observer.radiusChanged(radius);
        }
    }

    public boolean isNearCenter(double x, double y)
    {
        return Circles.distanceFromCenter(circle, x, y) < getPrecision();
    }
    public boolean isNearCircle(double x, double y)
    {
        if (isInSector(x, y))
        {
            return rawNearCircle(x, y);
        }
        else
        {
            return false;
        }
    }
    protected boolean rawNearCircle(double x, double y)
    {
        double radius = circle.getRadius();
        double distance = Circles.distanceFromCenter(circle, x, y);
        return Math.abs(distance - radius) < getPrecision();
    }
    public boolean isInCenter(double x, double y)
    {
        return Circles.distanceFromCenter(circle, x, y) < getPrecision()/2.0;
    }
    protected double getPrecision()
    {
        return getRadius()/5.0;
    }
    public Cursor getCursor(double x, double y)
    {
        if (isNearCenter(x, y))
        {
            return createCenterCursor(x, y);
        }
        if (isNearCircle(x, y))
        {
            return createRadiusCursor(x, y);
        }
        return null;
    }
    protected Cursor createCenterCursor(double x, double y)
    {
        return new CenterCursor();
    }
    protected Cursor createRadiusCursor(double x, double y)
    {
        return new RadiusCursor();
    }
    public interface Cursor
    {
        /**
         * Updates cursors position
         * <p>Note! Use returned cursor in future updates!
         * @param x
         * @param y
         * @return 
         */
        Cursor update(double x, double y);
        /**
         * Indicate finish of update.
         * @param x
         * @param y 
         */
        void ready(double x, double y);
    }
    protected class CenterCursor implements Cursor
    {
       @Override
        public Cursor update(double x, double y)
        {
            setX(x);
            setY(y);
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            if (isInCenter(x, y))
            {
                attached = true;
            }
            fireCenter(x, y);
        }

    }
    protected class RadiusCursor implements Cursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            setRadius(Circles.distanceFromCenter(circle, x, y));
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            fireRadius(getRadius());
        }
    }
    public interface MouldableCircleObserver
    {
        void centerMoved(double x, double y);
        void radiusChanged(double r);
    }
}