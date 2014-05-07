/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import java.io.Serializable;

/**
 *
 * @author tkv
 */
public class Scalar implements Comparable<Scalar>, Serializable
{
    private static final long serialVersionUID = 1L;
    public static final Scalar NaN = new Scalar(Double.NaN, ScalarType.UNKNOWN);
    /**
     * The maximum amount two scalars can differ to be equal
     */
    public static final double EPSILON = 0.0001;
    protected static final double KILO = 1000;
    protected static final double NM_IN_METERS = 1852;
    protected static final double FEET_IN_METERS = 0.3048;
    
    protected double _value;
    protected ScalarType type;
    
    protected Scalar()
    {
    }

    protected Scalar(ScalarType type)
    {
        this.type = type;
    }
    
    protected Scalar(double value, ScalarType type)
    {
        _value = value;
        this.type = type;
    }

    public Scalar(Scalar scalar)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        _value = scalar._value;
    }
    
    public void copy(Scalar scalar)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        _value = scalar._value;
    }

    public boolean isNan()
    {
        return Double.isNaN(_value);
    }
    
    public boolean equals(Scalar scalar, double maxDifference)
    {
        if (!type.equals(scalar.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        return Math.abs(_value - scalar._value) < maxDifference;
    }
    /**
     * This method returns true even when the value of two objects 
     * differ EPSILON amount
     * @param ob
     * @return true if ob is Scalar and abs(value - ob.value) < EPSILON
     * @see EPSILON
     */
    @Override
    public boolean equals(Object ob)
    {
        if (ob instanceof Scalar)
        {
            Scalar scalar = (Scalar) ob;
            return equals(scalar, EPSILON);
        }
        throw new UnsupportedOperationException("equals not possible with "+ob.getClass().getName());
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this._value) ^ (Double.doubleToLongBits(this._value) >>> 32));
        return hash;
    }
    
    public int compareTo(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        return Double.compare(_value, o._value);
    }
    /**
     * Add x to this
     * @param x
     */
    public void add(double x)
    {
        _value += x;
    }
    /**
     * Add o value to this
     * @param o
     */
    public void add(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        _value += o._value;
    }
    /**
     * Subtract x from this
     * @param x
     */
    public void subtract(double x)
    {
        _value -= x;
    }
    /**
     * Subtract o value from this
     * @param o
     */
    public void subtract(Scalar o)
    {
        if (!type.equals(o.type))
        {
            throw new UnsupportedOperationException("Action with wrong kind of class");
        }
        _value -= o._value;
    }
    /**
     * Multiply with x
     * @param x
     */
    public void mul(double x)
    {
        _value *= x;
    }
    /**
     * Add 1 to value
     */
    public void plusPlus()
    {
        _value++;
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
        return String.valueOf(_value);
    }

    public double getValue()
    {
        return _value;
    }
    
}
