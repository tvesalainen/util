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
public class Chain
{
    
    /**
     * Submerged weight per unit length N/M (w)
     * @param d In mm
     * @return
     */
    public static double chainWeight(double d)
    {
        return 0.1875 * d * d;
    }
    public final double w; // chain unit weight

    public Chain(double mm)
    {
        w = chainWeight(mm);
    }

    /**
     * Returns chain length
     * @param F Fairlead tension
     * @param d depth
     * @return
     */
    public double chainLength(double F, double d)
    {
        return Math.sqrt(d * (2 * F / w - d));
    }
    /**
     * Horizontal scope (length in plan view from fairlead to touchdown point)
     * @param T Fairlead tension
     * @param d Depth
     * @return 
     */
    public double horizontalScope(double T, double d)
    {
        return horizontalScope(T, d, chainLength(T, d));
    }
    /**
     * Horizontal scope (length in plan view from fairlead to touchdown point)
     * @param T Fairlead tension
     * @param d Depth
     * @param s Chain length
     * @return 
     */
    public double horizontalScope(double T, double d, double s)
    {
        double Fw = T / w;
        return (Fw - d) * Math.log((s + Fw) / (Fw - d));
    }

    public double horizontalForce(double d, double s)
    {
        double Tz = w * s;
        double T = fairleadTension(s, d);
        return Math.sqrt(T * T - Tz * Tz);
    }

    public double chainLengthForHorizontalForce(double Th, double d)
    {
        return MoreMath.solve(this::horizontalForce, d, Th, d, 10 * d);
    }

    /**
     * Returns fairlead tension
     * @param s
     * @param d
     * @return
     */
    public double fairleadTension(double s, double d)
    {
        return w * (s * s + d * d) / (2 * d);
    }
    
}
