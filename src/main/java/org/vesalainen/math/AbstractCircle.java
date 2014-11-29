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

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractCircle implements Circle
{
    private final Point center;
    private double radius;

    public AbstractCircle(Point center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }

    public AbstractCircle(Circle circle)
    {
        this.center = new AbstractPoint(circle.getX(), circle.getY());
        this.radius = circle.getRadius();
    }

    public AbstractCircle(double x, double y, double radius)
    {
        this.center = new AbstractPoint(x, y);
        this.radius = radius;
    }

    @Override
    public double getX()
    {
        return center.getX();
    }

    public void setX(double x)
    {
        if (center instanceof AbstractPoint)
        {
            AbstractPoint ap = (AbstractPoint) center;
            ap.setX(x);
        }
        else
        {
            throw new UnsupportedOperationException("optional method not supported");
        }
    }

    @Override
    public double getY()
    {
        return center.getY();
    }

    public void setY(double y)
    {
        if (center instanceof AbstractPoint)
        {
            AbstractPoint ap = (AbstractPoint) center;
            ap.setY(y);
        }
        else
        {
            throw new UnsupportedOperationException("optional method not supported");
        }
    }

    @Override
    public double getRadius()
    {
        return radius;
    }

    public void setRadius(double radius)
    {
        this.radius = radius;
    }
    
}
