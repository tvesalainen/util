/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.math.Unit;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html">Movable Type Scripts</a>
 */
public class GreatCircle
{
    private static final double R = 6371000;
    /**
     * Returns GC distance between (lat1, lon1) and (lat2, lon2)
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return NAUTICAL_MILE
     */
    @Unit(NAUTICAL_MILE)
    public static final double distance(double lat1, double lon1, double lat2, double lon2)
    {
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double λ1 = Math.toRadians(lon1);
        double λ2 = Math.toRadians(lon2);
        double Δφ = φ2 - φ1;
        double Δλ = λ2 - λ1;

        double a = square(Math.sin(Δφ/2))
          + Math.cos(φ1) * Math.cos(φ2)
          * square(Math.sin(Δλ/2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return METER.convertTo(R * c, NAUTICAL_MILE);        
    }
    /**
     * Returns GC initial bearing between (lat1, lon1) and (lat2, lon2)
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return DEGREE
     */
    @Unit(DEGREE)
    public static final double initialBearing(double lat1, double lon1, double lat2, double lon2)
    {
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double λ1 = Math.toRadians(lon1);
        double λ2 = Math.toRadians(lon2);
        
        double y = Math.sin(λ2-λ1) * Math.cos(φ2);
        double x = Math.cos(φ1)*Math.sin(φ2) -
                Math.sin(φ1)*Math.cos(φ2)*Math.cos(λ2-λ1);
        return Navis.normalizeAngle(Math.toDegrees(Math.atan2(y, x)));
    }

    private static double square(double x)
    {
        return x*x;
    }
}
