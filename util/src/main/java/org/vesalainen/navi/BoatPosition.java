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

import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.Meter;
import static org.vesalainen.math.UnitType.NM;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface BoatPosition
{

    /**
     * Returns boats getLength.
     * @return 
     */
    default double getLength()
    {
        return getDimensionToBow() + getDimensionToStern();
    }
    /**
     * Returns boats getBeam.
     * @return 
     */
    default double getBeam()
    {
        return getDimensionToStarboard() + getDimensionToPort();
    }

    /**
     * Returns center latitude with given coordinates and heading.
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    default double centerLatitude(double latitude, double longitude, double heading)
    {
        return latitudeAt(getBeam()/2, getLength()/2, latitude, heading);
    }

    /**
     * Returns center longitude with given coordinates and heading.
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    default double centerLongitude(double latitude, double longitude, double heading)
    {
        return longitudeAt(getBeam()/2, getLength()/2, latitude, longitude, heading);
    }

    /**
     * Returns latitude at pos with given this coordinates and heading.
     * @param pos
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    default double latitudeAt(BoatPosition pos, double latitude, double longitude, double heading)
    {
        if (getLength() != pos.getLength() || getBeam() != pos.getBeam())
        {
            throw new IllegalArgumentException("dimensions differ");
        }
        return latitudeAt(pos.getDimensionToPort(), pos.getDimensionToBow(), latitude, heading);
    }


    /**
     * Returns longitude at pos with given this coordinates and heading.
     * @param pos
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    default double longitudeAt(SimpleBoatPosition pos, double latitude, double longitude, double heading)
    {
        if (getLength() != pos.getLength() || getBeam() != pos.getBeam())
        {
            throw new IllegalArgumentException("dimensions differ");
        }
        return longitudeAt(pos.getDimensionToPort(), pos.getDimensionToBow(), latitude, longitude, heading);
    }
    
    default double latitudeAt(double toPort, double toBow, double latitude, double heading)
    {
        double bowDir = getDimensionToBow() - toBow; 
        double portDir = getDimensionToPort() - toPort;
        double meters = Math.hypot(bowDir, portDir);
        double angle = -Math.toDegrees(Math.atan2(portDir, bowDir));
        double bearing = Navis.normalizeAngle(angle+heading);
        return latitude + Navis.deltaLatitude(UnitType.convert(meters, Meter, NM), bearing);
    }
    default double longitudeAt(double toPort, double toBow, double latitude, double longitude, double heading)
    {
        double bowDir = getDimensionToBow() - toBow; 
        double portDir = getDimensionToPort() - toPort;
        double meters = Math.hypot(bowDir, portDir);
        double angle = -Math.toDegrees(Math.atan2(portDir, bowDir));
        double bearing = Navis.normalizeAngle(angle+heading);
        return longitude + Navis.deltaLongitude(latitude, UnitType.convert(meters, Meter, NM), bearing);
    }
    double getDimensionToStarboard();

    double getDimensionToPort();

    double getDimensionToBow();

    double getDimensionToStern();
    
}
