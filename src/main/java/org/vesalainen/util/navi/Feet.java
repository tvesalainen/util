/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import org.vesalainen.util.navi.Distance;

/**
 *
 * @author tkv
 */
public class Feet extends Distance
{
    public Feet(double feet)
    {
        super(feet*FEET_IN_METERS);
    }
}
