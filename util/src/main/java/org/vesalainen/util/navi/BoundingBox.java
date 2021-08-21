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

import java.util.Collection;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface BoundingBox<L>
{

    void add(BoundingBox<L> box);

    void add(Collection<L> locations);

    void add(L location);

    /**
     * @param latitude
     * @param longitude
     */
    void add(double latitude, double longitude);

    void clear();

    /**
     * Return area in square degrees
     * @return
     */
    double getArea();

    L getCenter();

    double getEast();

    /**
     * Return height in degrees
     * @return
     */
    double getHeight();

    double getNorth();

    L getNorthEast();

    double getSouth();

    L getSouthWest();

    double getWest();

    /**
     * Return width in degrees
     * @return
     */
    double getWidth();

    boolean isInside(BoundingBox<L> bb);

    boolean isInside(L pt);

    boolean isInside(double latitude, double longitude);

    boolean isIntersecting(BoundingBox<L> o);

    boolean overlapLatitude(double latitude);
    
    boolean overlapLongitude(double longitude);
    /**
     * Returns true if west lesser or equal than east
     * @return 
     */
    default boolean isWestToEast()
    {
        return getWest() <= getEast();
    }
    /**
     * Returns true if south lesser or equal than north
     * @return 
     */
    default boolean isSouthToNorth()
    {
        return getSouth() <= getNorth();
    }
    /**
     * Returns 2 or 1 bounding boxes possibly split at anti-meridian
     * @return 
     */
    BoundingBox[] splitAntiMeridian();
}
