/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;


/**
 *
 * @author tkv
 */
public class Miles extends Distance
{
    
    public Miles(double nauticalMiles)
    {
        super(nauticalMiles*NM_IN_METERS);
    }
    
    @Override
    public String toString()
    {
        return getMiles()+"NM";
    }
    
}
