/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.util.navi.Location;

/**
 * Area is a an area on map defined b locations.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Area
{
    protected Location[] locations;

    private Area(Location[] locations)
    {
        this.locations = locations;
    }

    public Location[] getLocations()
    {
        return locations;
    }
    
    protected double offset;
    /**
     * Returns true if location is inside area.
     * @param location
     * @return 
     */
    public boolean isInside(Location location)
    {
        return isInside(location.getLatitude(), location.getLongitude());
    }
    /**
     * Returns true if location is inside area.
     * @param latitude
     * @param longitude
     * @return 
     */
    public boolean isInside(double latitude, double longitude)
    {
        return isIn(latitude, longitude+offset);
    }
    protected final double convert(double longitude)
    {
        return (Navis.longitudeToGHA(longitude)+offset)%360;
    }
    protected abstract boolean isIn(double latitude, double longitude);
    /**
     * Returns rectangular area. Area is limited by lat/lon coordinats
     * @param latFrom
     * @param latTo
     * @param lonFrom
     * @param lonTo
     * @return 
     */
    public static Area getSquare(double latFrom, double latTo, double lonFrom, double lonTo)
    {
        Location[] locs = new Location[4];
        locs[0] = new Location(latFrom, lonFrom);
        locs[1] = new Location(latFrom, lonTo);
        locs[2] = new Location(latTo, lonFrom);
        locs[3] = new Location(latTo, lonTo);
        return new ConvexArea(locs);
    }
    /**
     * Returns area limited by given coordinates. Area must be convex.
     * @param locations
     * @return 
     */
    public static Area getConvex(Location... locations)
    {
        return new ConvexArea(locations);
    }
    public static class ConvexArea extends Area
    {
        private ConvexPolygon area;
        public ConvexArea(Location... locations)
        {
            super(locations);
            boolean[] quads = new boolean[4];
            for (Location loc : locations)
            {
                quads[(int)(convert(loc.getLongitude())/90)] = true;
            }
            int cnt = 0;
            for (boolean b : quads)
            {
                if (b)
                {
                    cnt++;
                }
            }
            if (cnt > 2)
            {
                throw new UnsupportedOperationException("over hemisphere not supported");
            }
            if (quads[0] && quads[3])
            {
                offset = 180;
            }
            area = new ConvexPolygon();
            for (Location loc : locations)
            {
                boolean added = area.addPoint(convert(loc.getLongitude()), loc.getLatitude());
                if (!added)
                {
                    throw new IllegalArgumentException("not convex");
                }
            }
        }

        @Override
        protected boolean isIn(double latitude, double longitude)
        {
            return area.isInside(longitude, latitude);
        }
        
    }   
}
