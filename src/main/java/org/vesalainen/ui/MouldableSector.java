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

import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.AbstractSector;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Circles;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableSector extends AbstractSector
{
    private AbstractPoint tempPoint;

    public MouldableSector(Circle circle)
    {
        super(circle);
    }
    /**
     * Detach sector center point from circle. Center updates won't effect 
     * original circle.
     */
    public void detachPoint()
    {
        if (tempPoint != null)
        {
            throw new IllegalStateException("already detached");
        }
        tempPoint = new AbstractPoint(circle);
    }
    /**
     * Attach to original circle.
     */
    public void attachPoint()
    {
        if (tempPoint == null)
        {
            throw new IllegalStateException("not detached");
        }
        tempPoint = null;
    }
    @Override
    public void setY(double y)
    {
        if (tempPoint == null)
        {
            tempPoint = new AbstractPoint(circle);
        }
        tempPoint.setY(y);
    }

    @Override
    public void setX(double x)
    {
        if (tempPoint == null)
        {
            tempPoint = new AbstractPoint(circle);
        }
        tempPoint.setX(x);
    }

    @Override
    public double getY()
    {
        if (tempPoint == null)
        {
            return super.getY();
        }
        else
        {
            return tempPoint.getY();
        }
    }

    @Override
    public double getX()
    {
        if (tempPoint == null)
        {
            return super.getX();
        }
        else
        {
            return tempPoint.getX();
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
            if (isCircle())
            {
                return new RadiusOrSplitCursor(x, y);
            }
            else
            {
                Cursor angleCursor = getAngleCursor(x, y);
                if (angleCursor != null)
                {
                    return angleCursor;
                }
                else
                {
                    return new RadiusCursor();
                }
            }
        }
        return null;
    }
    private Cursor getAngleCursor(double x, double y)
    {
        double precision = getRadius()/5.0;
        double dLeft = Circles.distance(getLeftX(), getLeftY(), x, y);
        double dRight = Circles.distance(getRightX(), getRightY(), x, y);
        if (dLeft < precision || dRight < precision)
        {
            if (dLeft < dRight)
            {
                return new LeftCursor();
            }
            else
            {
                return new RightCursor();
            }
        }
        else
        {
            return null;
        }
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
    private class CenterCursor implements Cursor
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
            double distance = Circles.distanceFromCenter(circle, x, y);
            if (distance < getRadius()/10.0)
            {
                attachPoint();
            }
        }
        
    }
    private class RadiusOrSplitCursor implements Cursor
    {
        private final double x0;
        private final double y0;

        public RadiusOrSplitCursor(double x0, double y0)
        {
            this.x0 = x0;
            this.y0 = y0;
        }
        
        @Override
        public Cursor update(double x, double y)
        {
            double ra = Circles.angle(getX(), getY(), x0, y0);
            double ta = Circles.angle(x0, y0, x, y);
            double diff = Math.abs(Angle.angleDiff(ra, ta));
            Cursor cursor;
            if (diff < Math.PI/4 || diff > Math.PI-Math.PI/4)
            {
                cursor = new RadiusCursor();
            }
            else
            {
                leftAngle = rightAngle = Circles.angle(getX(), getY(), x0, y0);
                double a = Circles.angle(getX(), getY(), x, y);
                if (Angle.clockwise(leftAngle, a))
                {
                    cursor = new RightCursor();
                }
                else
                {
                    cursor = new LeftCursor();
                }
            }
            cursor.update(x, y);
            return cursor;
        }

        @Override
        public void ready(double x, double y)
        {
        }
    }
    private abstract class AngleCursor implements Cursor
    {
        @Override
        public void ready(double x, double y)
        {
            double diff = Angle.angleDiff(leftAngle, rightAngle);
            if (Math.abs(diff) < Math.PI/8 )
            {
                leftAngle = rightAngle;
            }
        }
    }
    private class LeftCursor extends AngleCursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            double a = Circles.angle(getX(), getY(), x, y);
            leftAngle = a;
            return this;
        }
    }
    private class RightCursor extends AngleCursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            double a = Circles.angle(getX(), getY(), x, y);
            rightAngle = a;
            return this;
        }
    }
    private class RadiusCursor implements Cursor
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
        }
    }
}