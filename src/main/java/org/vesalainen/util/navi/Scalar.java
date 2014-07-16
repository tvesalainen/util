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
import java.util.concurrent.TimeUnit;

/**
 *
 * @author tkv
 */
public class Scalar implements Comparable<Scalar>, Serializable
{
    private static final long serialVersionUID = 2L;
    public static final Scalar NaN = new Scalar(Double.NaN, ScalarType.UNKNOWN);
    /**
     * The maximum amount two scalars can differ to be equal
     */
    public static final double Epsilon = 0.0001;
    protected static final double Kilo = 1000;
    protected static final double NMInMeters = 1852;
    protected static final double FeetInMeters = 0.3048;
    protected static final double FathomInMeters = 1.8288;
    protected static final double HoursInSecond = TimeUnit.HOURS.toSeconds(1);
    
    protected double value;
    protected ScalarType type;
    
    private Scalar()
    {
    }

    protected Scalar(ScalarType type)
    {
        this.type = type;
    }
    
    protected Scalar(double value, ScalarType type)
    {
        this.value = value;
        this.type = type;
    }

    public Scalar(Scalar scalar)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        value = scalar.value;
    }
    
    void copy(Scalar scalar)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        value = scalar.value;
    }

    public boolean isNan()
    {
        return Double.isNaN(value);
    }
    
    public boolean equals(Scalar scalar, double maxDifference)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        return Math.abs(value - scalar.value) < maxDifference;
    }
    /**
     * This method returns true even when the value of two objects 
 differ Epsilon amount
     * @param ob
     * @return true if ob is Scalar and abs(value - ob.value) < Epsilon
     * @see EPSILON#Epsilon
     */
    @Override
    public boolean equals(Object ob)
    {
        if (ob instanceof Scalar)
        {
            Scalar scalar = (Scalar) ob;
            return equals(scalar, Epsilon);
        }
        throw new UnsupportedOperationException("equals not possible with "+ob.getClass().getName());
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }
    
    @Override
    public int compareTo(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        return Double.compare(value, o.value);
    }
    /**
     * Add x to this
     * @param x
     */
    void add(double x)
    {
        value += x;
    }
    /**
     * Add o value to this
     * @param o
     */
    void add(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        value += o.value;
    }
    /**
     * Subtract x from this
     * @param x
     */
    void subtract(double x)
    {
        value -= x;
    }
    /**
     * Subtract o value from this
     * @param o
     */
    void subtract(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        value -= o.value;
    }
    /**
     * Multiply with x
     * @param x
     */
    void mul(double x)
    {
        value *= x;
    }
    /**
     * Add 1 to value
     */
    void plusPlus()
    {
        value++;
    }
    /**
     * True if this > o
     * @param o
     * @return
     */
    public boolean gt(Scalar o)
    {
        return compareTo(o) > 0;
    }
    /**
     * True if this < o
     * @param o
     * @return
     */
    public boolean lt(Scalar o)
    {
        return compareTo(o) < 0;
    }
    /**
     * True if this >= o
     * @param o
     * @return
     */
    public boolean ge(Scalar o)
    {
        return compareTo(o) >= 0;
    }
    /**
     * True if this <= o
     * @param o
     * @return
     */
    public boolean le(Scalar o)
    {
        return compareTo(o) <= 0;
    }
    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    public double getValue()
    {
        return value;
    }
    
}
