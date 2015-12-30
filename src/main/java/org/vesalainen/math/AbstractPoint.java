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
public class AbstractPoint implements Point, Serializable
{
    private static final long serialVersionUID = 1L;
    protected double x;
    protected double y;

    public AbstractPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public AbstractPoint(Point point)
    {
        this.x = point.getX();
        this.y = point.getY();
    }

    @Override
    public double getX()
    {
        return x;
    }

    public void set(Point point)
    {
        set(point.getX(), point.getY());
    }
    
    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
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
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AbstractPoint other = (AbstractPoint) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
        {
            return false;
        }
        return true;
    }
    
}
