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
        }
        attachPoint.setX(x);
        attachPoint.setY(y);
    }

    @Override
    public double getX()
    {
        return circle.getX();
    }

    public void setX(double x)
    {
        attached = false;
        circle.setX(x);
    }

    @Override
    public double getY()
    {
        return circle.getY();
    }

    public void setY(double y)
    {
        attached = false;
        circle.setY(y);
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

    public void addObserver(MouldableCircleObserver observer)
    {
        observers.add(observer);
    }
    
    public void removeObserver(MouldableCircleObserver observer)
    {
        observers.remove(observer);
    }
    
    private void fireCenter(double x, double y)
    {
        for (MouldableCircleObserver observer : observers)
        {
            observer.centerMoved(x, y);
        }
    }

    private void fireRadius(double radius)
    {
        for (MouldableCircleObserver observer : observers)
        {
            observer.radiusChanged(radius);
        }
    }
    
    public Cursor getCursor(double x, double y)
    {
        double distance = Circles.distance(getX(), getY(), x, y);
        double precision = getRadius()/5.0;
        if (distance < precision)
        {
            return new CenterCursor();
        }
        if (Math.abs(distance - circle.getRadius()) < precision)
        {
            return new RadiusCursor();
        }
        return null;
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
            double distance = Circles.distanceFromCenter(attachPoint, x, y);
            if (distance < getRadius()/10.0)
            {
                circle.set(attachPoint);
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
            setRadius(Circles.distance(getX(), getY(), x, y));
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