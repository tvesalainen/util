/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util.navi;

import java.io.Serializable;


/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Motion implements Serializable
{
    private static final long serialVersionUID = 2L;
    private Velocity speed;
    private Angle angle;

    public Motion()
    {
        speed = new Velocity();
        angle = new Angle();
    }
    
    public Motion(Velocity speed, Angle angle)
    {
        this.speed = speed;
        this.angle = angle;
    }
    
    public Motion(Velocity speed, Angle angle, boolean relative)
    {
        this.speed = speed;
        this.angle = angle;
        if (relative)
        {
            this.angle = this.angle.halfAngle();
        }
    }
    
    /**
     * Creates a new Motion object by combining other two
     * @param m1
     * @param m2
     */
    public Motion(Motion m1, Motion m2)
    {
        setXY(m1.getX()+m2.getX(), m1.getY()+m2.getY());
    }
    
    /**
     * Creates a new Motion object by combining other two
     * @param m1
     * @param m2
     * @param relative If true the angle is normalized between 0 - 180
     */
    public Motion(Motion m1, Motion m2, boolean relative)
    {
        setXY(m1.getX()+m2.getX(), m1.getY()+m2.getY());
        if (relative)
        {
            angle = angle.halfAngle();
        }
    }
    
    public Motion turn(Angle angle, boolean clockwice)
    {
        return new Motion(speed, this.angle.add(angle, clockwice));
    }
    /**
     *
     * @param x x-component m/s
     * @param y y-component m/s
     */
    public Motion(double x, double y)
    {
        setXY(x, y);
    }
    
    protected void setXY(double x, double y)
    {
        speed = new Velocity(Math.sqrt(x*x+y*y));
        angle = new Angle(x, y);
    }
    
    protected double getX()
    {
        return cos()*speed.getValue();
    }
    
    protected double getY()
    {
        return sin()*speed.getValue();
    }
    /**
     * Creates a counter motion. Speed component stays the same but angle component
     * points to opposite direction.
     * @return
     */
    public Motion getCounter()
    {
        return new Motion(speed, angle.straightAngle());
    }
    
    /**
     * Creates a relative motion. Speed component stays the same but angle component
     * is normalized between 0 - 180
     * @return
     */
    public Motion getRelative()
    {
        return new Motion(speed, angle.halfAngle());
    }
    
    public Velocity getSpeed()
    {
        return speed;
    }

    public Angle getAngle()
    {
        return angle;
    }
    
    /**
     * 
     * @return In radians
     */
    public double getRadians()
    {
        return angle.getRadians();
    }
    
    public double getDegree()
    {
        return angle.getDegree();
    }
    
    public double getMetersInSecond()
    {
        return speed.getMetersInSecond();
    }

    public double getKiloMetersInHour()
    {
        return speed.getKiloMetersInHour();
    }

    public double getKnots()
    {
        return speed.getKnots();
    }

    /**
     * 
     * @return cos
     * @see java.lang.Math#cos
     */
    public double cos()
    {
        return angle.cos();
    }
    
    /**
     * 
     * @return sin
     * @see java.lang.Math#sin
     */
    public double sin()
    {
        return angle.sin();
    }
    
    @Override
    public boolean equals(Object ob)
    {
        if (ob instanceof Motion)
        {
            Motion wind = (Motion) ob;
            return speed.equals(wind.speed) && angle.equals(wind.angle);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + (this.speed != null ? this.speed.hashCode() : 0);
        hash = 71 * hash + (this.angle != null ? this.angle.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return speed+" "+angle;
    }



}
