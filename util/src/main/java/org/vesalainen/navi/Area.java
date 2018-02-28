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

import java.util.Arrays;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.util.navi.Angle;
import org.vesalainen.util.navi.Location;

/**
 * Area is a an area on map defined b locations.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Area
{
    protected Location[] locations;
    protected double offset;

    private Area(Location... locations)
    {
        this.locations = locations;
    }
    /**
     * Returns copy of original locations
     * @return 
     */
    public Location[] getLocations()
    {
        return Arrays.copyOf(locations, locations.length);
    }
    
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
        return isIn(latitude, convert(longitude));
    }
    protected final double convert(double longitude)
    {
        return ((longitude > 0 ? 360-longitude : -longitude)+offset+360)%360;
    }
    protected abstract boolean isIn(double latitude, double longitude);
    /**
     * Returns area limited by given coordinates. Area must be convex.
     * If exactly 2 locations are given the longitudes must be 0 defining polar
     * area limited by latitudes.
     * @param locations
     * @return 
     */
    public static Area getArea(Location... locations)
    {
        if (locations.length == 2)
        {
            if (locations[0].getLongitude() == 0.0 && locations[1].getLongitude() == 0.0)
            {
                return getPolar(locations[0].getLatitude(), locations[1].getLatitude());
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        else
        {
            return new ConvexArea(locations);
        }
    }
    /**
     * Returns rectangular area. Area is limited by lat/lon coordinates
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
     * Returns rectangular area. Area is limited by lat/lon coordinates. 
     * Parameter midLon determines which side of earth area lies.
     * @param latFrom
     * @param latTo
     * @param lonFrom
     * @param midLon
     * @param lonTo
     * @return 
     */
    public static Area getPolar(double latFrom, double latTo, double lonFrom, double midLon, double lonTo)
    {
        Location[] locs = new Location[5];
        locs[0] = new Location(latFrom, lonFrom);
        locs[1] = new Location(latFrom, lonTo);
        locs[2] = new Location(latTo, lonFrom);
        locs[3] = new Location(latTo, lonTo);
        locs[4] = new Location(latFrom, midLon);
        return new ConvexArea(locs);
    }
    /**
     * Returns rectangular polar area. Area is limited by lat coordinates. 
     * @param latFrom
     * @param latTo
     * @return 
     */
    public static Area getPolar(double latFrom, double latTo)
    {
        return new PolarArea(latFrom, latTo);
    }
    public static class PolarArea extends Area
    {
        private double latFrom;
        private double latTo;
        
        public PolarArea(double latFrom, double latTo)
        {
            super(new Location(latFrom, 0), new Location(latTo, 0));
            if (latFrom < latTo)
            {
                this.latFrom = latFrom;
                this.latTo = latTo;
            }
            else
            {
                this.latFrom = latTo;
                this.latTo = latFrom;
            }
        }

        @Override
        public boolean isInside(double latitude, double longitude)
        {
            return isIn(latitude, longitude);
        }
        
        @Override
        protected boolean isIn(double latitude, double longitude)
        {
            return latitude >= latFrom && latitude <= latTo;
        }
        
    }
    public static class ConvexArea extends Area
    {
        private ConvexPolygon area;
        public ConvexArea(Location... locations)
        {
            super(locations);
            Angle[] lons = new Angle[locations.length];
            int index = 0;
            for (Location loc : locations)
            {
                lons[index++] = new Angle(Math.toRadians(Navis.longitudeToGHA(loc.getLongitude())));
            }
            Angle average = Angle.average(lons);
            offset = 180-average.getDegree();
            area = new ConvexPolygon();
            for (Location loc : locations)
            {
                boolean added = area.addPoint(convert(loc.getLongitude()), loc.getLatitude());
            }
            if (!area.isConvex())
            {
                throw new IllegalArgumentException("not convex");
            }
        }

        @Override
        protected boolean isIn(double latitude, double longitude)
        {
            return area.isInside(longitude, latitude);
        }
        
    }   
}
