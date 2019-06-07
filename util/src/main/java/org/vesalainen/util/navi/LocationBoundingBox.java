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
package org.vesalainen.util.navi;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationBoundingBox extends AbstractBoundingBox<Location>
{

    public LocationBoundingBox()
    {
        super(LocationSupport.LOCATION_SUPPORT);
    }

    public LocationBoundingBox(Location point)
    {
        super(LocationSupport.LOCATION_SUPPORT, point);
    }

    public LocationBoundingBox(Location northEast, Location southWest)
    {
        super(LocationSupport.LOCATION_SUPPORT, northEast, southWest);
    }

    public LocationBoundingBox(double latitude, double longitude, double dia)
    {
        super(LocationSupport.LOCATION_SUPPORT, latitude, longitude, dia);
    }

    public LocationBoundingBox(String southWestNorthEast)
    {
        super(LocationSupport.LOCATION_SUPPORT, southWestNorthEast);
    }

    public LocationBoundingBox(double north, double east, double south, double west)
    {
        super(LocationSupport.LOCATION_SUPPORT, north, east, south, west);
    }
    
}
