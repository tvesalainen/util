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
public class BasicSafeSector implements  Serializable, SafeSector
{
    private static final long serialVersionUID = 1L;
    private final InnerCircle innerCircle;
    private Point point;
    private final AbstractCircle attachCircle;
    private AbstractPoint detachPoint;
    private double innerRadius;
    private double rightX;
    private double rightY;
    private double leftX;
    private double leftY;

    public BasicSafeSector(AbstractCircle circle)
    {
        this.point = attachCircle = circle;
        this.innerCircle = new InnerCircle();
        innerRadius = circle.getRadius() / 2.0;
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

    private void updateSector()
    {
        setLeftAngle(getLeftAngle());
        setRightAngle(getRightAngle());
    }
    public void setLeftAngle(double radians)
    {
        double radius = getRadius();
        leftX = radius*Math.cos(radians);
        leftY = radius*Math.sin(radians);
    }
    
    public void setRightAngle(double radians)
    {
        double radius = getRadius();
        rightX = radius*Math.cos(radians);
        rightY = radius*Math.sin(radians);
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
        updateSector();
    }

    @Override
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
    
    public void set(double x, double y)
    {
        if (detachPoint == null)
        {
            point = detachPoint = new AbstractPoint(attachCircle);
        }
        detachPoint.set(x, y);
    }

    public boolean isInside(double x, double y)
    {
        if (isCircle())
        {
            return Circles.isInside(this, x, y);
        }
        else
        {
            return (Circles.isInside(this, x, y) && isInSector(x, y)) ||
                    Circles.isInside(innerCircle, x, y);
        }
    }
    
    public boolean isInSector(double x, double y)
    {
        if (isCircle())
        {
            return true;
        }
        return rawIsInSector(x, y);
    }
    private boolean rawIsInSector(double x, double y)
    {
        double ox = x-getX();
        double oy = y-getY();
        if (Vectors.isClockwise(leftX, leftY, rightX, rightY))
        {
            return Vectors.isClockwise(leftX, leftY, ox, oy) && Vectors.isClockwise(ox, oy, rightX, rightY);
        }
        else
        {
            return !(Vectors.isClockwise(rightX, rightY, ox, oy) && Vectors.isClockwise(ox, oy, leftX, leftY));
        }
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
            double ox = x-getX();
            double oy = y-getY();
            if (Circles.distance(leftX, leftY, ox, oy) < r)
            {
                return new LeftCursor(r);
            }
            if (Circles.distance(rightX, rightY, ox, oy) < r)
            {
                return new RightCursor(r);
            }
            if (rawIsInSector(x, y))
            {
                if (distanceFromCenter - getRadius() < r)
                {
                    return new RadiusCursor();
                }
            }
            else
            {
                if (distanceFromCenter - innerRadius < r)
                {
                    return new InnerRadiusCursor(r);
                }
            }
        }
        else
        {
            if (distanceFromCenter - getRadius() < r)
            {
                return new RadiusOrAngleCursor(x, y, r);
            }
        }
        return null;
    }
    public class CenterCursor implements Cursor
    {
        final double r;
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
        final double x;
        final double y;
        final double r;

        public RadiusOrAngleCursor(double x, double y, double r)
        {
            this.x = x-getX();
            this.y = y-getY();
            this.r = r;
        }

        @Override
        public Cursor update(double x, double y)
        {
            double ox = x-getX();
            double oy = y-getY();
            double distance = Circles.distance(this.x, this.y, ox, oy);
            if (distance > r)
            {
                Cursor cursor;
                if (Math.abs(Circles.distanceFromCenter(point, x, y)-getRadius()) > r/2.0)
                {
                    cursor = new RadiusCursor();
                }
                else
                {
                    if (Vectors.isClockwise(this.x, this.y, ox, oy))
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
            innerRadius = Math.min(innerRadius, getRadius());
        }

    }
    public abstract class AngleCursor implements Cursor
    {
        final double r;

        public AngleCursor(double r)
        {
            this.r = r;
        }
        
        @Override
        public void ready(double x, double y)
        {
            updateSector();
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
            leftX = x-getX();
            leftY = y-getY();
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
            rightX = x-getX();
            rightY = y-getY();
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            super.ready(x, y);
        }
        
    }
    public class InnerCircle implements Circle, Serializable
    {
        private static final long serialVersionUID = 1L;

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
    public class InnerRadiusCursor implements Cursor
    {
        double r;

        public InnerRadiusCursor(double r)
        {
            this.r = r;
        }
        
        @Override
        public Cursor update(double x, double y)
        {
            double ir = Circles.distanceFromCenter(point, x, y);
            if (ir < getRadius() && ir > r)
            {
                innerRadius = ir;
            }
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            if (getRadius() - innerRadius < r)
            {
                leftX = leftY = rightX = rightY = 0;
                innerRadius = getRadius() / 2.0;
            }
        }

    }
}
