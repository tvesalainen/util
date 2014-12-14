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

import org.vesalainen.math.AbstractSector;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Circles;
import org.vesalainen.math.Sector;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableSector extends MouldableCircle implements Sector
{
    protected AbstractSector sector;

    public MouldableSector(Circle circle)
    {
        this(new AbstractSector(circle));
    }
    
    public MouldableSector(AbstractSector sector)
    {
        super(sector);
        this.sector = (AbstractSector) circle;
    }

    @Override
    public boolean isInside(double x, double y)
    {
        return sector.isInside(x, y);
    }

    public double getLeftX()
    {
        return sector.getLeftX();
    }

    public double getLeftY()
    {
        return sector.getLeftY();
    }

    public double getRightX()
    {
        return sector.getRightX();
    }

    public double getRightY()
    {
        return sector.getRightY();
    }

    @Override
    public boolean isCircle()
    {
        return sector.isCircle();
    }

    @Override
    public double getAngle()
    {
        return sector.getAngle();
    }

    @Override
    public double getLeftAngle()
    {
        return sector.getLeftAngle();
    }

    public void setLeftAngle(double leftAngle)
    {
        sector.setLeftAngle(leftAngle);
    }

    @Override
    public double getRightAngle()
    {
        return sector.getRightAngle();
    }

    public void setRightAngle(double rightAngle)
    {
        sector.setRightAngle(rightAngle);
    }
    
    private void fireLeft(double leftAngle)
    {
        for (MouldableCircleObserver observer : observers)
        {
            if (observer instanceof MouldableSectorObserver)
            {
                MouldableSectorObserver mso = (MouldableSectorObserver) observer;
                mso.leftAngleChanged(leftAngle);
            }
        }
    }
    private void fireRight(double rightAngle)
    {
        for (MouldableCircleObserver observer : observers)
        {
            if (observer instanceof MouldableSectorObserver)
            {
                MouldableSectorObserver mso = (MouldableSectorObserver) observer;
                mso.rightAngleChanged(rightAngle);
            }
        }
    }
    @Override
    public Cursor getCursor(double x, double y)
    {
        double distance = Circles.distanceFromCenter(this, x, y);
        double precision = getRadius()/5.0;
        if (distance < precision)
        {
            return new CenterCursor();
        }
        if (Math.abs(distance - getRadius()) < precision)
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
    protected Cursor getAngleCursor(double x, double y)
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

    protected class RadiusOrSplitCursor implements Cursor
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
                double angle = Circles.angle(getX(), getY(), x0, y0);
                sector.setLeftAngle(angle);
                sector.setRightAngle(angle);
                double a = Circles.angle(getX(), getY(), x, y);
                if (Angle.clockwise(angle, a))
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
    protected abstract class AngleCursor implements Cursor
    {
        @Override
        public void ready(double x, double y)
        {
            double diff = Angle.angleDiff(sector.getLeftAngle(), sector.getRightAngle());
            if (Math.abs(diff) < Math.PI/8 )
            {
                sector.makeCircle();
            }
        }
    }
    protected class LeftCursor extends AngleCursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            double a = Circles.angle(getX(), getY(), x, y);
            sector.setLeftAngle(a);
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            super.ready(x, y);
            fireLeft(getLeftAngle());
        }
        
    }
    protected class RightCursor extends AngleCursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            double a = Circles.angle(getX(), getY(), x, y);
            sector.setRightAngle(a);;
            return this;
        }
        @Override
        public void ready(double x, double y)
        {
            super.ready(x, y);
            fireRight(getRightAngle());
        }
        
    }
    public interface MouldableSectorObserver extends MouldableCircleObserver
    {
        void leftAngleChanged(double leftAngle);
        void rightAngleChanged(double rightAngle);
    }
}