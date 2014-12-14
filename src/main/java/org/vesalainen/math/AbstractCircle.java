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

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractCircle extends AbstractPoint implements Circle, Serializable
{
    private static final long serialVersionUID = 1L;
    protected double radius;

    public AbstractCircle(Point center, double radius)
    {
        super(center);
        this.radius = radius;
    }

    public AbstractCircle(Circle circle)
    {
        super(circle);
        this.radius = circle.getRadius();
    }

    public AbstractCircle(double x, double y, double radius)
    {
        super(x, y);
        this.radius = radius;
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

    boolean isInside(double x, double y)
    {
        return Circles.isInside(this, x, y);
    }
    
}
