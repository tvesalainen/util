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

import java.io.Serializable;
import java.util.Collection;
import org.vesalainen.lang.Primitives;
import org.vesalainen.navi.Navis;

/**
 * <p>Note! Bounding box covers newer more than half of sphere.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractBoundingBox<L> extends AbstractLocationSupport<L> implements Serializable, BoundingBox<L>
{
    protected static final long serialVersionUID = 1L;
    
    protected static final double HalfCircle = 180;
    protected static final double FullCircle = 360;
    private boolean init;
    private double north;
    private double south;
    private double west;
    private double east;


    protected AbstractBoundingBox()
    {
        super(null);
    }

    protected AbstractBoundingBox(AbstractLocationSupport support)
    {
        super(support);
    }

    protected AbstractBoundingBox(AbstractLocationSupport support, L point)
    {
        this(support, point, point);
    }
    protected AbstractBoundingBox(AbstractLocationSupport support, L northEast, L southWest)
    {
        this(
                support,
                support.latitudeSupplier.applyAsDouble(northEast), 
                support.longitudeSupplier.applyAsDouble(northEast), 
                support.latitudeSupplier.applyAsDouble(southWest), 
                support.longitudeSupplier.applyAsDouble(southWest)
        );
    }
    /**
     * 
     * @param support
     * @param center
     * @param dia diameter in NM
     */
    protected AbstractBoundingBox(AbstractLocationSupport support, L center, double dia)
    {
        this(
                support,
                support.latitudeSupplier.applyAsDouble(center), 
                support.longitudeSupplier.applyAsDouble(center), 
                dia
        );
    }
    /**
     * 
     * @param latitude
     * @param longitude
     * @param dia diameter in NM
     */
    protected AbstractBoundingBox(AbstractLocationSupport support, double latitude, double longitude, double dia)
    {
        this(support, normalize(latitude+dia/60), normalize(longitude+dia/60), normalize(latitude-dia/60), normalize(longitude-dia/60));
    }

    protected AbstractBoundingBox(AbstractLocationSupport support, String southWestNorthEast)
    {
        this(support, split(southWestNorthEast));
    }
    protected AbstractBoundingBox(AbstractLocationSupport support, String[] s)
    {
        this(support, Primitives.parseDouble(s[2]), Primitives.parseDouble(s[3]), Primitives.parseDouble(s[0]), Primitives.parseDouble(s[1]));
    }
    protected AbstractBoundingBox(AbstractLocationSupport support, double north, double east, double south, double west)
    {
        super(support);
        add(north, east);
        add(south, west);
    }
    
    private static String[] split(String southWestNorthEast)
    {
        String[] split = southWestNorthEast.split(",");
        if (split.length != 4)
        {
            throw new IllegalArgumentException(southWestNorthEast);
        }
        return split;
    }
    protected static final double normalize(double val)
    {
        return ((val + HalfCircle + FullCircle) % FullCircle) - HalfCircle;
    }

    /**
     * Return true if east right of west
     * @param west
     * @param east
     * @return
     */
    protected static final boolean isWestToEast(double west, double east)
    {
        west += HalfCircle;
        east += HalfCircle;
        double d = east - west;
        if (Math.abs(d) <= HalfCircle)
        {
            return d >= 0;
        }
        else
        {
            return d < 0;
        }
    }
    /**
     * Return height in degrees
     * @return
     */
    @Override
    public double getHeight()
    {
        return north - south;
    }

    /**
     * Return width in degrees
     * @return
     */
    @Override
    public double getWidth()
    {
        if (east >= west)
        {
            return east - west;
        }
        else
        {
            return 360 + east - west;
        }
    }

    @Override
    public void add(BoundingBox<L> box)
    {
        add(box.getSouth(), box.getWest());
        add(box.getNorth(), box.getEast());
    }

    @Override
    public void add(Collection<L> locations)
    {
        for (L location : locations)
        {
            add(location);
        }
    }

    @Override
    public void add(L location)
    {
        add(latitudeSupplier.applyAsDouble(location), longitudeSupplier.applyAsDouble(location));
    }

    /**
     * @param latitude
     * @param longitude
     */
    @Override
    public void add(double latitude, double longitude)
    {
        if (
                (latitude < -90) ||
                (latitude > 90) ||
                (longitude < -180) ||
                (longitude > 180)
                )
        {
            throw new IllegalArgumentException("illegal coordinates");
        }
        if (init)
        {
            north = Math.max(north, latitude);
            south = Math.min(south, latitude);
            east = isWestToEast(east, longitude) ? longitude : east;
            west = !isWestToEast(west, longitude) ? longitude : west;
        }
        else
        {
            north = latitude;
            south = latitude;
            west = longitude;
            east = longitude;
            init = true;
        }
    }

    @Override
    public boolean isIntersecting(BoundingBox<L> o)
    {
        return (overlapLatitude(o.getNorth()) || overlapLatitude(o.getSouth()) || o.overlapLatitude(north) || o.overlapLatitude(south)) && (overlapLongitude(o.getWest()) || overlapLongitude(o.getEast()) || o.overlapLongitude(west) || o.overlapLongitude(east));
    }

    @Override
    public boolean overlapLatitude(double latitude)
    {
        return latitude <= north && latitude >= south;
    }

    @Override
    public boolean overlapLongitude(double longitude)
    {
        return  isWestToEast(longitude, east) &&
                isWestToEast(west, longitude);
    }

    @Override
    public boolean isInside(BoundingBox<L> bb)
    {
        return isInside(bb.getSouth(), bb.getWest()) && isInside(bb.getNorth(), bb.getEast());
    }

    @Override
    public boolean isInside(L pt)
    {
        return isInside(latitudeSupplier.applyAsDouble(pt), longitudeSupplier.applyAsDouble(pt));
    }

    @Override
    public boolean isInside(double latitude, double longitude)
    {
        return overlapLatitude(latitude) && overlapLongitude(longitude);
    }

    @Override
    public void clear()
    {
        north = 0;
        south = 0;
        west = 0;
        east = 0;
        init = false;
    }

    /**
     * Return area in square degrees
     * @return
     */
    @Override
    public double getArea()
    {
        double abs = Math.abs(east - west);
        if (abs > HalfCircle)
        {
            abs = FullCircle - abs;
        }
        return abs * (north - south) * Math.cos(Math.toRadians((north + south) / 2));
    }

    @Override
    public L getSouthWest()
    {
        return locationFactory.create(south, west);
    }

    @Override
    public L getNorthEast()
    {
        return locationFactory.create(north, east);
    }

    @Override
    public L getCenter()
    {
        return Navis.locationCenter(locationFactory, south, west, north, east);
    }

    @Override
    public double getNorth()
    {
        return north;
    }

    @Override
    public double getSouth()
    {
        return south;
    }

    @Override
    public double getWest()
    {
        return west;
    }

    @Override
    public double getEast()
    {
        return east;
    }

    @Override
    public BoundingBox[] splitAntiMeridian()
    {
        if (isWestToEast())
        {
            return new BoundingBox[]{this};
        }
        else
        {
            return new BoundingBox[]{
                new AbstractBoundingBox(this, north, 180, south, west),
                new AbstractBoundingBox(this, north, east, south, -180)
            };
        }
    }

    @Override
    public String toString()
    {
        return "{" + "north=" + north + ", south=" + south + ", west=" + west + ", east=" + east + '}';
    }
    
}
