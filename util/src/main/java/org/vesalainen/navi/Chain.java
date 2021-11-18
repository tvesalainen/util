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

import static java.lang.Math.*;
import org.vesalainen.math.MoreMath;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Chain
{
    public final double w; // chain unit weight
    private final double maxChainLength;
    /**
     * Creates new Chain.
     * @param mm Chain thickness in mm.
     */
    public Chain(double mm)
    {
        this(mm, Double.MAX_VALUE);
    }
    /**
     * Creates new Chain.
     * @param mm Chain thickness in mm.
     * @param maxChainLength Available chain length
     */
    public Chain(double mm, double maxChainLength)
    {
        this.w = chainWeight(mm);
        this.maxChainLength = maxChainLength;
    }

    /**
     * Submerged weight per unit length N/M (w)
     * @param d Chain diameter in mm
     * @return
     */
    public static double chainWeight(double d)
    {
        return 0.1875 * d * d;
    }

    /**
     * Returns catenary part of chain length. This is the minimum chain length
     * with tension and depth.
     * @param T Fairlead tension
     * @param d depth
     * @return
     */
    public double chainLength(double T, double d)
    {
        return sqrt(d * (2 * T / w + d));
    }
    /**
     * Horizontal scope (length in plan view from fairlead to chain touchdown point)
     * @param T Fairlead tension
     * @param d Depth
     * @return 
     */
    public double horizontalScope(double T, double d)
    {
        return horizontalScope(T, d, chainLength(T, d));
    }
    /**
     * Horizontal scope (length in plan view from fairlead to chain touchdown point)
     * @param T Fairlead tension
     * @param d Depth
     * @param s Catenary part of chain length
     * @return 
     */
    public double horizontalScope(double T, double d, double s)
    {
        double Fw = T / w;
        return (Fw - d) * Math.log((s + Fw) / (Fw - d));
    }

    /**
     * Returns Fairlead horizontal tension
     * @param s
     * @param d
     * @return
     */
    public double fairleadTension(double s, double d)
    {
        if (s < d)
        {
            throw new IllegalArgumentException("chain shorter that depth");
        }
        return w * (s * s - d * d) / (2 * d);
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
        return Math.sqrt(T * T - Th * Th) / w;
    }

    /**
     * Vertical force at the fairlead
     * @param s Chain length
     * @return
     */
    public double verticalForce(double s)
    {
        return w * s;
    }
    public double maximalDepth(double T, double s)
    {
        double b = 2*T/w;
        double c = -s*s;
        return (-b+sqrt(b*b-4*c))/2;
    }
}
