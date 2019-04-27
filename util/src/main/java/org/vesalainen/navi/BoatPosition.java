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
import static org.vesalainen.math.UnitType.*;

/**
 * BoatPosition stores a position in boat. Examples are GPS, Radar, etc. BoatPosition
 * is used in making coordinate transforms. 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatPosition
{
    private double toSb;
    private double toPort;
    private double toBow;
    private double toStern;
    /**
     * Creates BoatPosition
     * @param toSb Meters to starboard side.
     * @param toPort Meters to port side.
     * @param toBow Meters to bow.
     * @param toStern  Meters to stern.
     */
    public BoatPosition(double toSb, double toPort, double toBow, double toStern)
    {
        this.toSb = toSb;
        this.toPort = toPort;
        this.toBow = toBow;
        this.toStern = toStern;
    }
    /**
     * Returns boats length.
     * @return 
     */
    public double length()
    {
        return toBow + toStern;
    }
    /**
     * Returns boats beam.
     * @return 
     */
    public double beam()
    {
        return toSb + toPort;
    }
    /**
     * Returns center latitude with given coordinates and heading.
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    public double centerLatitude(double latitude, double longitude, double heading)
    {
        return latitudeAt(beam()/2, length()/2, latitude, longitude, heading);
    }
    /**
     * Returns latitude at pos with given this coordinates and heading.
     * @param pos
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    public double latitudeAt(BoatPosition pos, double latitude, double longitude, double heading)
    {
        if (length() != pos.length() || beam() != pos.beam())
        {
            throw new IllegalArgumentException("dimensions differ");
        }
        return latitudeAt(pos.toPort, pos.toBow, latitude, longitude, heading);
    }
    private double latitudeAt(double toPort, double toBow, double latitude, double longitude, double heading)
    {
        double bowDir = this.toBow - toBow; 
        double portDir = this.toPort - toPort;
        double meters = Math.hypot(bowDir, portDir);
        double angle = -Math.toDegrees(Math.atan2(portDir, bowDir));
        double bearing = Navis.normalizeAngle(angle+heading);
        return latitude + Navis.deltaLatitude(UnitType.convert(meters, Meter, NM), bearing);
    }
    /**
     * Returns center longitude with given coordinates and heading.
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    public double centerLongitude(double latitude, double longitude, double heading)
    {
        return longitudeAt(beam()/2, length()/2, latitude, longitude, heading);
    }
    /**
     * Returns longitude at pos with given this coordinates and heading.
     * @param pos
     * @param latitude
     * @param longitude
     * @param heading
     * @return 
     */
    public double longitudeAt(BoatPosition pos, double latitude, double longitude, double heading)
    {
        if (length() != pos.length() || beam() != pos.beam())
        {
            throw new IllegalArgumentException("dimensions differ");
        }
        return longitudeAt(pos.toPort, pos.toBow, latitude, longitude, heading);
    }
    private double longitudeAt(double toPort, double toBow, double latitude, double longitude, double heading)
    {
        double bowDir = this.toBow - toBow; 
        double portDir = this.toPort - toPort;
        double meters = Math.hypot(bowDir, portDir);
        double angle = -Math.toDegrees(Math.atan2(portDir, bowDir));
        double bearing = Navis.normalizeAngle(angle+heading);
        return longitude + Navis.deltaLongitude(latitude, UnitType.convert(meters, Meter, NM), bearing);
    }
}
