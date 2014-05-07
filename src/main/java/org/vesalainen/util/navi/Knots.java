/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author tkv
 */
public class Knots extends Velocity
{
    public Knots(double knots)
    {
        super(Miles.NM_IN_METERS*knots/TimeUnit.HOURS.toSeconds(1));
    }
    
    @Override
    public String toString()
    {
        return String.format("%.1fKn", getKnots());
    }
    
}

