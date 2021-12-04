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
import java.util.function.DoubleToIntFunction;
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
    private final DoubleToIntFunction lon;
    private final DoubleToIntFunction lat;
    
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
        this.lon = (lo)->(int) floor((lo/departure)/this.boxSize);
        this.lat = (la)->(int) floor(la/this.boxSize);
    }

    public V put(double longitude, double latitude, V value)
    {
        return map.put(lon.applyAsInt(longitude), lat.applyAsInt(latitude), value);
    }
    public V get(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude));
    }
    public V getOrCreate(double longitude, double latitude)
    {
        return map.getOrCreate(lon.applyAsInt(longitude), lat.applyAsInt(latitude));
    }
    public V getNorth(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude)+1);
    }
    public V getNorthEast(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude)+1);
    }
    public V getNorthWest(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude)+1);
    }
    public V getSouth(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude)-1);
    }
    public V getSouthEast(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude)-1);
    }
    public V getSouthWest(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude)-1);
    }
    public V getEast(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude));
    }
    public V getWest(double longitude, double latitude)
    {
        return map.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude));
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
