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
public class AbstractSector extends AbstractCircle implements Sector, Serializable
{
    private static final long serialVersionUID = 1L;
    protected double leftAngle;
    protected double rightAngle;

    public AbstractSector(Circle circle)
    {
        super(circle);
    }

    public AbstractSector(Circle circle, double leftAngle, double rightAngle)
    {
        super(circle);
        this.leftAngle = leftAngle;
        this.rightAngle = rightAngle;
    }

    @Override
    public boolean isInside(double x, double y)
    {
        return super.isInside(x, y) && isInSector(x, y);
    }
    
    public boolean isInSector(double x, double y)
    {
        if (isCircle())
        {
            return true;
        }
        double angle = Circles.angle(this, x, y);
        if (getAngle() <= Math.PI)
        {
            return Angle.clockwise(rightAngle, angle) && Angle.clockwise(angle, leftAngle);
        }
        else
        {
            return !(Angle.clockwise(leftAngle, angle) && Angle.clockwise(angle, rightAngle));
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

    public void makeCircle()
    {
        this.leftAngle = this.rightAngle = 0.0;
    }

}
