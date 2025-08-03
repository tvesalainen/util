/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum CardinalDirection
{
    /**
     * North
     */
    N,
    /**
     * North-northeast
     */
    NNE,
    /**
     * Northeast
     */
    NE,
    /**
     * East-northeast
     */
    ENE,
    /**
     * East
     */
    E,
    /**
     * East-southeast
     */
    ESE,
    /**
     * Southeast
     */
    SE,
    /**
     * South-southeast
     */
    SSE,
    /**
     * South
     */
    S,
    /**
     * South-southwest
     */
    SSW,
    /**
     * Southwest
     */
    SW,
    /**
     * West southwest
     */
    WSW,
    /**
     * West
     */
    W,
    /**
     * West-northwest
     */
    WNW,
    /**
     * Northwest
     */
    NW,
    /**
     * North-northwest
     */
    NNW
    ;
    /**
     * Returns cardinal direction. (N, E, S, W)
     * @param degree
     * @return 
     */
    public static CardinalDirection cardinal(double degree)
    {
        double m = Navis.normalizeAngle(degree+45);
        int d = (int) floor(m/90);
        return values()[d*4];
    }
    /**
     * Returns intercardinal direction. (N, NE, E, SE, ...)
     * @param degree
     * @return 
     */
    public static CardinalDirection interCardinal(double degree)
    {
        double m = Navis.normalizeAngle(degree+22.5);
        int d = (int) floor(m/45);
        return values()[d*2];
    }
    /**
     * Returns secondary intercardinal direction. (N, NNE, NE, ENE, E, ESE, SE, ...)
     * @param degree
     * @return 
     */
    public static CardinalDirection secondaryInterCardinal(double degree)
    {
        double m = Navis.normalizeAngle(degree+11.25);
        int d = (int) floor(m/22.5);
        return values()[d];
    }
}
