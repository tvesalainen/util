/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.io.Serializable;

/**
 * Provides support for efficient longitude operations in proximity distance.
 * Internal longitudes are projected by using start latitude. Calculating 
 * distances and bearings, for example, is possible without departure. 
 * Additionally this utility handles pacific -180/180 longitude edge.
 * @author Timo Vesalainen
 */
public class LocalLongitude implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    protected double departure;

    private LocalLongitude(double latitude)
    {
        this.departure = Math.cos(Math.toRadians(latitude));
    }
    /**
     * Returns LocalLongitude instance which is usable about 60 NM around starting
     * point.
     * @param longitude
     * @param latitude
     * @return 
     */
    public static LocalLongitude getInstance(double longitude, double latitude)
    {
        if (Math.abs(longitude) < 179)
        {
            return new LocalLongitude(latitude);
        }
        else
        {
            return new PacificLongitude(latitude);
        }
    }
    /**
     * Returns internal representation of longitude. It can be used to calculate
     * distances and bearing as in cartesian coordinate within proximity of starting
     * point.
     * <p>Returned value has no meaning outside it's LocalLongitude instance.
     * @param longitude Real-world longitude.
     * @return 
     */
    public double getInternal(double longitude)
    {
        return longitude * departure;
    }
    /**
     * Returns real world longitude from internal form created by the same 
     * LocalLongitude instance.
     * @param longitude Internal form longitude.
     * @return 
     */
    public double getExternal(double longitude)
    {
        return longitude / departure;
    }
    
    private static class PacificLongitude extends LocalLongitude
    {

        private PacificLongitude(double latitude)
        {
            super(latitude);
        }

        @Override
        public double getExternal(double longitude)
        {
            longitude = super.getExternal(longitude);
            if (longitude < -180)
            {
                longitude = 360+longitude;
            }
            return longitude;
        }

        @Override
        public double getInternal(double longitude)
        {
            if (longitude > 0)
            {
                longitude = -360+longitude;
            }
            return super.getInternal(longitude);
        }
        
        
    }
}
