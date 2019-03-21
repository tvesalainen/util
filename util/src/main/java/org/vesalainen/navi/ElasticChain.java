/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import org.vesalainen.math.MoreMath;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ElasticChain extends Chain
{ // chain unit weight
    // chain unit weight
    public final double AE;   // chain stffness

    public ElasticChain(double mm)
    {
        super(mm);
        this.AE = chainStiffness(mm);
    }
    /**
     * Axial stiffness per unit length N (AE)
     * @param d In mm
     * @return 
     */
    public static double chainStiffness(double d)
    {
        return 90000*d*d;
    }
    /**
     * Horizontal force for a given fairlead tension T
     * @param T Fairlead tension
     * @param d depth
     * @return 
     */
    public double horizontalForce(Double T, double d)
    {
        return AE*Math.sqrt(Math.pow(T/AE+1, 2)-2*w*d/AE)-AE;
    }
    /**
     * Minimum line length required (or suspended length for a given fairlead 
     * tension) for gravity anchor:
     * @param T Fairlead tension
     * @param Th Horizontal force
     * @return 
     */
    public double minimalLineLength(double T, double Th)
    {
        return Math.sqrt(T*T-Th*Th)/w;
    }
    /**
     * Vertical force at the fairlead
     * @param s Chain length
     * @return 
     */
    public double verticalForce(double s)
    {
        return w*s;
    }
}
