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
public class CatenaryAnchoring
{
    public double w;   // chain unit weight
    public double AE;   // chain stffness

    public CatenaryAnchoring(double mm)
    {
        this.w = chainWeight(mm);
        this.AE = chainStiffness(mm);
    }
    /**
     * Returns chain length
     * @param F Fairlead tension
     * @param d depth
     * @return 
     */
    public double chainLength(double F, double d)
    {
        return Math.sqrt(d*(2*F/w-d));
    }
    public double horizontalDistance(double F, double d)
    {
        return horizontalDistance(F, d, chainLength(F, d));
    }
    public double horizontalDistance(double F, double d, double s)
    {
        double Fw = F/w;
        return (Fw-d)*Math.log((s+Fw)/(Fw-d));
    }
    public double horizontalForce(double d, double s)
    {
        double Tz = w*s;
        double T = fairleadTension(s, d);
        return Math.sqrt(T*T-Tz*Tz);
    }
    public double chainLengthForHorizontalForce(double Th, double d)
    {
        return MoreMath.solve(this::horizontalForce, d, Th, d, 10*d);
    }
    /**
     * Returns fairlead tension
     * @param s
     * @param d
     * @return 
     */
    public double fairleadTension(double s, double d)
    {
        return w*(s*s+d*d)/(2*d);
    }
    /**
     * Submerged weight per unit length N/M (w)
     * @param d In mm
     * @return 
     */
    public static double chainWeight(double d)
    {
        return 0.1875*d*d;
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
}
