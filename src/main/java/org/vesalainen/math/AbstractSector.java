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

package org.vesalainen.math;

import java.io.Serializable;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractSector implements Sector, Serializable
{
    private static final long serialVersionUID = 1L;
    protected Circle circle;
    protected double leftAngle;
    protected double rightAngle;

    public AbstractSector(Circle circle)
    {
        this.circle = circle;
    }

    public AbstractSector(Circle circle, double leftAngle, double rightAngle)
    {
        this.circle = circle;
        this.leftAngle = leftAngle;
        this.rightAngle = rightAngle;
    }

    public boolean isInside(double x, double y)
    {
        if (!Circles.isInside(circle, x, y))
        {
            return false;
        }
        if (isCircle())
        {
            return true;
        }
        double angle = Circles.angle(circle, x, y);
        return Angle.clockwise(rightAngle, angle) && Angle.clockwise(angle, leftAngle);
    }
    @Override
    public double getX()
    {
        return circle.getX();
    }

    @Override
    public double getY()
    {
        return circle.getY();
    }

    @Override
    public double getRadius()
    {
        return circle.getRadius();
    }

    public void setX(double x)
    {
        if (circle instanceof AbstractCircle)
        {
            AbstractCircle ac = (AbstractCircle) circle;
            ac.setX(x);
        }
        else
        {
            throw new UnsupportedOperationException("optional method not supported");
        }
    }

    public void setY(double y)
    {
        if (circle instanceof AbstractCircle)
        {
            AbstractCircle ac = (AbstractCircle) circle;
            ac.setY(y);
        }
        else
        {
            throw new UnsupportedOperationException("optional method not supported");
        }
    }

    public void setRadius(double radius)
    {
        if (circle instanceof AbstractCircle)
        {
            AbstractCircle ac = (AbstractCircle) circle;
            ac.setRadius(radius);
        }
        else
        {
            throw new UnsupportedOperationException("optional method not supported");
        }
    }
    
    public double getLeftX()
    {
        return getX()+Math.cos(leftAngle)*getRadius();
    }
    
    public double getLeftY()
    {
        return getY()+Math.sin(leftAngle)*getRadius();
    }
    
    public double getRightX()
    {
        return getX()+Math.cos(rightAngle)*getRadius();
    }
    
    public double getRightY()
    {
        return getY()+Math.sin(rightAngle)*getRadius();
    }
    
    @Override
    public boolean isCircle()
    {
        return leftAngle == rightAngle;
    }
    @Override
    public double getAngle()
    {
        return Angle.normalizeToFullAngle(Angle.angleDiff(rightAngle, leftAngle));
    }

    @Override
    public double getLeftAngle()
    {
        return leftAngle;
    }

    public void setLeftAngle(double leftAngle)
    {
        this.leftAngle = leftAngle;
    }

    @Override
    public double getRightAngle()
    {
        return rightAngle;
    }

    public void setRightAngle(double rightAngle)
    {
        this.rightAngle = rightAngle;
    }

}
