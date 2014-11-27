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
    private double x;
    private double y;
    private double radius;

    public AbstractCircle(Circle circle)
    {
        this.x = circle.getX();
        this.y = circle.getY();
        this.radius = circle.getRadius();
    }

    public AbstractCircle(double x, double y, double radius)
    {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    @Override
    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
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
