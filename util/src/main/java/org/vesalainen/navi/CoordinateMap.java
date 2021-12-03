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

import static java.lang.Math.*;
import java.util.function.Supplier;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.util.Map2D;
import org.vesalainen.util.TreeMap2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateMap<V>
{
    private final double departure;
    private final double boxSize;
    private final double boxSize2;
    private final Map2D<Integer,Integer,V> map;
    
    public CoordinateMap(double latitude, double boxSize, UnitType unit)
    {
        this(latitude, boxSize, unit, null);
    }
    public CoordinateMap(double latitude, double boxSize, UnitType unit, Supplier<V> squareCreator)
    {
        this.departure = cos(toRadians(latitude));
        this.map = new TreeMap2D<>(squareCreator);
        this.boxSize = unit.convertTo(boxSize, NAUTICAL_DEGREE);
        this.boxSize2 = boxSize/2;
    }

    public V put(double longitude, double latitude, V value)
    {
        int lon = (int) floor((longitude/departure)/boxSize);
        int lat = (int) floor(latitude/boxSize);
        return map.put(lon, lat, value);
    }
    public V get(double longitude, double latitude)
    {
        int lon = (int) floor((longitude/departure)/boxSize);
        int lat = (int) floor(latitude/boxSize);
        return map.get(lon, lat);
    }
    
    public void forEach(CoordinateConsumer<V> act)
    {
        map.forEach((lon,lat,v)->
        {
            act.accept((lon+boxSize2)*departure*boxSize, (lat+boxSize2)*boxSize, v);
        });
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        forEach((double lon, double lat, V v)->
        {
            sb.append('\n');
            sb.append(CoordinateFormat.formatLongitude(lon, COORDINATE_DEGREES_AND_MINUTES));
            sb.append(", ");
            sb.append(CoordinateFormat.formatLatitude(lat, COORDINATE_DEGREES_AND_MINUTES));
            sb.append(", ");
            sb.append(v);
        });
        return sb.toString();
    }
    @FunctionalInterface
    public interface CoordinateConsumer<V>
    {
        void accept(double longitude, double latitude, V value);
    }
}
