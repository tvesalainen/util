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

import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Circles;
import org.vesalainen.math.Point;
import org.vesalainen.math.Sector;
import org.vesalainen.math.Vectors;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class SafeSector implements Sector
{
    private final InnerCircle innerCircle;
    private Point point;
    private final AbstractCircle attachCircle;
    private AbstractPoint detachPoint;
    private double innerRadius;
    private double rightX;
    private double rightY;
    private double leftX;
    private double leftY;

    public SafeSector(AbstractCircle circle)
    {
        this.point = attachCircle = circle;
        this.innerCircle = new InnerCircle();
    }
    
    @Override
    public double getAngle()
    {
        return Angle.normalizeToFullAngle(Angle.angleDiff(getRightAngle(), getLeftAngle()));
    }

    @Override
    public double getLeftAngle()
    {
        return Angle.normalizeToFullAngle(Math.atan2(leftY, leftX));
    }

    @Override
    public double getRightAngle()
    {
        return Angle.normalizeToFullAngle(Math.atan2(rightY, rightX));
    }

    @Override
    public boolean isCircle()
    {
        return Double.compare(leftX, rightX) == 0 && Double.compare(leftY, rightY) == 0;
    }

    @Override
    public double getRadius()
    {
        return attachCircle.getRadius();
    }

    private void setRadius(double r)
    {
        attachCircle.setRadius(r);
    }

    public Circle getInnerCircle()
    {
        return innerCircle;
    }
    
    @Override
    public double getX()
    {
        return point.getX();
    }

    @Override
    public double getY()
    {
        return point.getY();
    }
    
    void set(double x, double y)
    {
        if (detachPoint == null)
        {
            point = detachPoint = new AbstractPoint(attachCircle);
        }
        detachPoint.set(x, y);
    }

    public Cursor getCursor(double x, double y, double r)
    {
        double distanceFromCenter = Circles.distanceFromCenter(this, x, y);
        if (distanceFromCenter < r)
        {
            return new CenterCursor(r);
        }
        if (!isCircle())
        {
            
        }
        if (distanceFromCenter - getRadius() < r)
        {
            return new RadiusOrAngleCursor(x, y, r);
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
    public class CenterCursor implements Cursor
    {
        private final double r;
        private CenterCursor(double r)
        {
            this.r = r;
        }
        
       @Override
        public Cursor update(double x, double y)
        {
            set(x, y);
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            if (Circles.distance(attachCircle, detachPoint) < r/3.0)
            {
                point = attachCircle;
                detachPoint = null;
            }
        }

    }
    public class RadiusOrAngleCursor implements Cursor
    {
        private final double x;
        private final double y;
        private final double r;

        public RadiusOrAngleCursor(double x, double y, double r)
        {
            this.x = x;
            this.y = y;
            this.r = r;
        }

        @Override
        public Cursor update(double x, double y)
        {
            double distance = Circles.distance(this.x, this.y, x, y);
            if (distance > r)
            {
                Cursor cursor;
                if (Math.abs(Circles.distanceFromCenter(point, x, y)-getRadius()) > r/2.0)
                {
                    cursor = new RadiusCursor();
                }
                else
                {
                    if (Vectors.isClockwise(this.x, this.y, x, y))
                    {
                        cursor = new LeftCursor(r);
                        rightX = this.x;
                        rightY = this.y;
                    }
                    else
                    {
                        cursor = new RightCursor(r);
                        leftX = this.x;
                        leftY = this.y;
                    }
                }
                cursor.update(x, y);
                return cursor;
            }
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
        }
        
    }
    public class RadiusCursor implements Cursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            setRadius(Circles.distanceFromCenter(point, x, y));
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
        }

    }
    public abstract class AngleCursor implements Cursor
    {
        private final double r;

        public AngleCursor(double r)
        {
            this.r = r;
        }
        
        @Override
        public void ready(double x, double y)
        {
            double radius = getRadius();
            double leftAngle = getLeftAngle();
            leftX = radius*Math.cos(leftAngle);
            leftY = radius*Math.sin(leftAngle);
            double rightAngle = getRightAngle();
            rightX = radius*Math.cos(rightAngle);
            rightY = radius*Math.sin(rightAngle);
            if (Circles.distance(leftX, leftY, rightX, rightY) < r)
            {
                leftX = leftY = rightX = rightY = 0;
            }
        }
        
    }
    public class LeftCursor extends AngleCursor
    {

        public LeftCursor(double r)
        {
            super(r);
        }

        @Override
        public Cursor update(double x, double y)
        {
            leftX = x;
            leftY = y;
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            super.ready(x, y);
        }
        
    }
    public class RightCursor extends AngleCursor
    {

        public RightCursor(double r)
        {
            super(r);
        }

        @Override
        public Cursor update(double x, double y)
        {
            rightX = x;
            rightY = y;
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            super.ready(x, y);
        }
        
    }
    public class InnerCircle implements Circle
    {

        @Override
        public double getRadius()
        {
            return innerRadius;
        }

        @Override
        public double getX()
        {
            return point.getX();
        }

        @Override
        public double getY()
        {
            return point.getY();
        }
        
    }
}
