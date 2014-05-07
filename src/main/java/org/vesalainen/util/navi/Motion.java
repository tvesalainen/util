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
public class Motion implements Comparable<Scalar>, Serializable
{
    private static final long serialVersionUID = 1L;
    private Velocity _speed;
    private Angle _angle;

    public Motion()
    {
        _speed = new Velocity();
        _angle = new Angle();
    }
    
    public Motion(Velocity speed, Angle angle)
    {
        _speed = speed;
        _angle = angle;
    }
    
    public Motion(Velocity speed, Angle angle, boolean relative)
    {
        _speed = speed;
        _angle = angle;
        if (relative)
        {
            _angle = _angle.halfAngle();
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
            _angle = _angle.halfAngle();
        }
    }
    
    public Motion turn(Angle angle, boolean clockwice)
    {
        return new Motion(_speed, _angle.add(angle, clockwice));
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
        _speed = new Velocity(Math.sqrt(x*x+y*y));
        _angle = new Angle(x, y);
    }
    
    protected double getX()
    {
        return cos()*_speed.getValue();
    }
    
    protected double getY()
    {
        return sin()*_speed.getValue();
    }
    /**
     * Creates a counter motion. Speed component stays the same but angle component
     * points to opposite direction.
     * @return
     */
    public Motion getCounter()
    {
        return new Motion(_speed, _angle.straightAngle());
    }
    
    /**
     * Creates a relative motion. Speed component stays the same but angle component
     * is normalized between 0 - 180
     * @return
     */
    public Motion getRelative()
    {
        return new Motion(_speed, _angle.halfAngle());
    }
    
    public Velocity getSpeed()
    {
        return _speed;
    }

    public Angle getAngle()
    {
        return _angle;
    }
    
    /**
     * 
     * @return In radians
     */
    public double getRadians()
    {
        return _angle.getRadians();
    }
    
    public double getDegree()
    {
        return _angle.getDegree();
    }
    
    public double getMetersInSecond()
    {
        return _speed.getMetersInSecond();
    }

    public double getKiloMetersInHour()
    {
        return _speed.getKiloMetersInHour();
    }

    public double getKnots()
    {
        return _speed.getKnots();
    }

    /**
     * 
     * @return cos
     * @see java.lang.Math.cos
     */
    public double cos()
    {
        return _angle.cos();
    }
    
    /**
     * 
     * @return sin
     * @see java.lang.Math.sin
     */
    public double sin()
    {
        return _angle.sin();
    }
    
    @Override
    public boolean equals(Object ob)
    {
        if (ob instanceof Motion)
        {
            Motion wind = (Motion) ob;
            return _speed.equals(wind._speed) && _angle.equals(wind._angle);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + (this._speed != null ? this._speed.hashCode() : 0);
        hash = 71 * hash + (this._angle != null ? this._angle.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return _speed+" "+_angle;
    }

    public int compareTo(Scalar o)
    {
        if (o instanceof Velocity)
        {
            Velocity vv = (Velocity) o;
            return Double.compare(_speed._value, vv._value);
        }
        if (o instanceof Angle)
        {
            Angle aa = (Angle) o;
            return Double.compare(_angle._value, aa._value);
        }
        throw new UnsupportedOperationException("compareTo not possible with "+o.getClass().getName());
    }

    public void setSpeed(Velocity speed)
    {
        this._speed = speed;
    }

    public void setAngle(Angle angle)
    {
        this._angle = angle;
    }

}
