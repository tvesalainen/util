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

import org.vesalainen.math.AbstractCircle;
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
    private Circle safeCircle;
    private final double precision;

    public MouldableSector(Circle circle, double precision)
    {
        super(circle);
        this.precision = precision;
    }
    
    public void detachCircle()
    {
        if (safeCircle != null)
        {
            throw new IllegalStateException("already detached");
        }
        safeCircle = circle;
        circle = new AbstractCircle(safeCircle);
    }
    public void attachCircle()
    {
        if (safeCircle == null)
        {
            throw new IllegalStateException("not detached");
        }
        circle = safeCircle;
        safeCircle = null;
    }
    public boolean isAttached()
    {
        return safeCircle != null;
    }
    
    public Cursor getCursor(double x, double y)
    {
        double distance = Circles.distanceFromCenter(circle, x, y);
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
        
    }
    private class RadiusOrSplitCursor implements Cursor
    {
        private double x0;
        private double y0;

        public RadiusOrSplitCursor(double x0, double y0)
        {
            this.x0 = x0;
            this.y0 = y0;
        }
        
        @Override
        public Cursor update(double x, double y)
        {
            double d0 = Circles.distanceFromCenter(circle, x0, y0);
            double d1 = Circles.distanceFromCenter(circle, x, y);
            double d2 = Circles.distance(x0, y0, x, y);
            double rd = Math.abs(d0-d1)*2;
            Cursor cursor;
            if (d2 > rd)
            {
                cursor = new RadiusCursor();
            }
            else
            {
                double a0 = Circles.angle(circle, x0, y0);
                double a = Circles.angle(circle, x, y);
                if (Angle.clockwise(a0, a))
                {
                    cursor = new LeftCursor();
                }
                else
                {
                    cursor = new RightCursor();
                }
            }
            cursor.update(x, y);
            return cursor;
        }
    }
    private class LeftCursor implements Cursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            leftAngle = Circles.angle(circle, x, y);
            return this;
        }
    }
    private class RightCursor implements Cursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            rightAngle = Circles.angle(circle, x, y);
            return this;
        }
    }
    private class RadiusCursor implements Cursor
    {
        @Override
        public Cursor update(double x, double y)
        {
            setRadius(Circles.distanceFromCenter(circle, x, y));
            return this;
        }
    }
}