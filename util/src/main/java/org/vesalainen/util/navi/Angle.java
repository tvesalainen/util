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


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Angle extends Scalar
{
    public static final Angle Right = new Degree(90);
    public static final Angle Straight = new Degree(180);
    public static final Angle North = new Degree(0);
    public static final Angle East = new Degree(90);
    public static final Angle South = new Degree(180);
    public static final Angle West = new Degree(270);
    public static final Angle NE = new Degree(45);
    public static final Angle SE = new Degree(135);
    public static final Angle NW = new Degree(315);
    public static final Angle SW = new Degree(225);
    public static final double FULL_CIRCLE = 2*Math.PI;
    /**
     * Creates a Angle class
     * @param radians Angle in radians
     */
    public Angle(double radians)
    {
        super(normalizeToFullAngle(radians), ScalarType.ANGLE);
    }
    /**
     * Creates a Angle class
     * @param x Param x in rectangular coordinates
     * @param y Param y in rectangular coordinates
     * @see java.lang.Math#atan2
     */
    public Angle(double x, double y)
    {
        super(normalizeToFullAngle(Math.atan2(y, x)), ScalarType.ANGLE);
    }

    public Angle()
    {
        super(ScalarType.ANGLE);
    }
    /**
     * 
     * @return In radians
     */
    public double getRadians()
    {
        return value;
    }
    /**
     * The angle in degrees
     * @return
     */
    public double getDegree()
    {
        return Math.toDegrees(value);
    }
    /**
     * The right angle. Meaning this angle minus 90 degrees
     * @return
     */
    public Angle rightAngle()
    {
        return add(Angle.Right, false);
    }
    
    /**
     * The "left" angle. Meaning this angle plus 90 degrees
     * @return
     */
    public Angle leftAngle()
    {
        return add(Angle.Right);
    }
    
    /**
     * The straight angle. Meaning this angle plus 180 degrees
     * @return
     */
    public Angle straightAngle()
    {
        return add(Angle.Straight);
    }
    /**
     * 
     * @return Returns the angle so that angles greater than 180 degrees are changed
     * to be under 180. New angle in 360 - old
     */
    public Angle halfAngle()
    {
        return new Angle(normalizeToHalfAngle(value));
    }
    
    /**
     * Add angle clockwise.
     * @param angle
     * @return Returns a new Angle 
     */
    public Angle add(Angle angle)
    {
        return add(angle, true);
    }
    
    /**
     * Add angle clockwise or counter clockwice.
     * @param angle
     * @return Returns a new Angle 
     */
    public Angle add(Angle angle, boolean clockwice)
    {
        if (clockwice)
        {
            return new Angle(normalizeToFullAngle(value + angle.value));
        }
        else
        {
            return new Angle(normalizeToFullAngle(value - angle.value));
        }
    }
    /**
     * Turn angle clockwise or counter clockwice. If &lt; 180 turns
     * clockwice. If &ge; 180 turns counter clockwice 360 - angle
     * @param angle
     * @return Returns a new Angle 
     */
    public Angle turn(Angle angle)
    {
        if (angle.getRadians() < Math.PI)
        {
            return add(angle, true);
        }
        else
        {
            return add(angle.toHalfAngle(), false);
        }
    }
    /** 
     * @return Angle normalized between 0 - 180 degrees
     */
    public Angle toHalfAngle()
    {
        return new Angle(normalizeToHalfAngle(value));
    }
    /**
     * Sector is less than 180 degrees delimited by start and end
     * @param start
     * @param end
     * @return If this is between start - end
     */
    public final boolean inSector(Angle start, Angle end)
    {
        if (start.clockwise(end))
        {
            return !clockwise(start) && clockwise(end);
        }
        else
        {
            return clockwise(start) && !clockwise(end);
        }
    }

    public static final Angle average(Angle... angles)
    {
        double sin = 0;
        double cos = 0;
        for (Angle angle : angles)
        {
            sin += angle.sin();
            cos += angle.cos();
        }
        return new Angle(cos, sin);
    }
    /**
     * The difference between two angles
     * @param a1
     * @param a2
     * @return New Angle representing the difference
     */
    public static final Angle difference(Angle a1, Angle a2)
    {
        return new Angle(normalizeToHalfAngle(angleDiff(a1.value, a2.value)));
    }

    public boolean equals(Angle angle, double maxDifference)
    {
        return angleDiff(value, angle.value) < maxDifference;
    }
    /**
     * 10 is clockwise from 340
     * @param angle
     * @return true if angle is clockwise from this angle
     */
    public final boolean clockwise(Angle angle)
    {
        return clockwise(value, angle.value);
    }
    /**
     * 10 is clockwise from 340
     * @param angle1
     * @param angle2
     * @return true if angle2 is clockwise from angle1
     */
    public static final boolean clockwise(Angle angle1, Angle angle2)
    {
        return clockwise(angle1.value, angle2.value);
    }
    /**
     * Returns true if angle1 is clockwise of angle2
     * @param angle1
     * @param angle2
     * @return 
     */
    public static final boolean clockwise(double angle1, double angle2)
    {
        return angleDiff(angle1, angle2) >= 0;
    }

    /**
     * 
     * @return acos
     * @see java.lang.Math#acos(double) 
     */
    public final double acos()
    {
        return Math.acos(value);
    }
    /**
     * 
     * @return asin
     * @see java.lang.Math#asin
     */
    public final double asin()
    {
        return Math.asin(value);
    }
    
    /**
     * 
     * @return atan
     * @see java.lang.Math#atan
     */
    public double atan()
    {
        return Math.atan(value);
    }
    
    /**
     * 
     * @return cos
     * @see java.lang.Math#cos
     */
    public double cos()
    {
        return Math.cos(value);
    }
    
    /**
     * 
     * @return cosh
     * @see java.lang.Math#cosh
     */
    public double cosh()
    {
        return Math.cosh(value);
    }
    
    /**
     * 
     * @return sin
     * @see java.lang.Math#sin
     */
    public double sin()
    {
        return Math.sin(value);
    }
    
    /**
     * 
     * @return sinh
     * @see java.lang.Math#sinh
     */
    public double sinh()
    {
        return Math.sinh(value);
    }
    
    @Override
    public String toString()
    {
        return Math.round(getDegree())+"\u00B0";
    }
    
    /**
     * @param angle in radians
     * @return Angle normalized between 0 - 180 degrees
     */
    public static final double normalizeToHalfAngle(double angle)
    {
        angle = normalizeToFullAngle(angle);
        if (angle > Math.PI)
        {
            return FULL_CIRCLE - angle;
        }
        assert angle >= 0 && angle <= Math.PI;
        return angle;
    }
    
    /**
     * @param angle in radians
     * @return angle normalized between 0 - 360 degrees
     */
    public  static final double normalizeToFullAngle(double angle)
    {
        if (angle > FULL_CIRCLE)
        {
            angle -= FULL_CIRCLE;
        }
        if (angle < 0)
        {
            angle = FULL_CIRCLE + angle;
        }
        assert angle >= 0 && angle <= 2*Math.PI;
        return angle;
    }
    /**
     * Convert full angle to signed angle -180 - 180. 340 -&gt; -20
     * @param angle
     * @return
     */
    public  static final double signed(double angle)
    {
        angle = normalizeToFullAngle(angle);
        if (angle > Math.PI)
        {
            return angle - FULL_CIRCLE;
        }
        else
        {
            return angle;
        }
    }
    /**
     * @param anAngle1 in radians
     * @param anAngle2 in radians
     * @return Angle difference normalized between 0 - PI radians. If anAngle2 is right to anAngle1 returns + signed
     */
    public static final double angleDiff(double anAngle1, double anAngle2)
    {
        double angle;
        anAngle1 = normalizeToFullAngle(anAngle1);
        anAngle2 = normalizeToFullAngle(anAngle2);
        angle = anAngle2 - anAngle1;
        angle = normalizeToFullAngle(angle);
        return signed(angle);
    }

}
