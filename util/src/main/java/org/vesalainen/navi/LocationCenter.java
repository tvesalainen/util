/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationCenter
{
    private double latitudeSum = 0;
    private double sin = 0;
    private double cos = 0;
    private double count;
    
    public void add(double longitude, double latitude)
    {
        add(longitude, latitude, 1);
    }
    public void add(double longitude, double latitude, double weight)
    {
        if (Double.isFinite(longitude) && Double.isFinite(latitude) && Double.isFinite(weight))
        {
            latitudeSum += latitude*weight;
            double rad = Math.toRadians(longitude);
            sin += Math.sin(rad)*weight;
            cos += Math.cos(rad)*weight;
            count += weight;
        }
        else
        {
            throw new IllegalArgumentException("");
        }
    }
    
    public double latitude()
    {
        return latitudeSum/count;
    }
    public double longitude()
    {
        double atan2 = Math.atan2(sin, cos);
        return Navis.normalizeToHalfAngle(Math.toDegrees(atan2));
    }

    @Override
    public String toString()
    {
        return Navis.longitudeString(longitude())+" "+Navis.latitudeString(latitude());
    }
    
}
