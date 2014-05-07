/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import org.vesalainen.util.navi.Angle;

/**
 *
 * @author tkv
 */
public class Degree extends Angle
{
    public Degree(double value)
    {
        super(Math.toRadians(value));
    }
    
    public Degree(int value)
    {
        super(Math.toRadians(value));
    }

    @Override
    public void plusPlus()
    {
        _value += Math.toRadians(1);
    }
}
