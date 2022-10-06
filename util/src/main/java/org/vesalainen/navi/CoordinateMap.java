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
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.function.BiFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntToDoubleFunction;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.util.Merger;
import org.vesalainen.util.TreeMap2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateMap<V> extends TreeMap2D<Integer,Integer,V>
{
    private final double departure;
    private final double boxSize;
    private final double boxSize2;
    private final DoubleToIntFunction lon;
    private final DoubleToIntFunction lat;
    private final IntToDoubleFunction invlon;
    private final IntToDoubleFunction invlat;
    private final BiFunction<Double,Double,V> squareCreator;
    
    public CoordinateMap(double latitude, double boxSize, UnitType unit)
    {
        this(latitude, boxSize, unit, null);
    }
    public CoordinateMap(double latitude, double boxSize, UnitType unit, BiFunction<Double,Double,V> squareCreator)
    {
        this.departure = cos(toRadians(latitude));
        this.boxSize = unit.convertTo(boxSize, NAUTICAL_DEGREE);
        this.boxSize2 = this.boxSize/2;
        this.lon = (lo)->(int) floor((lo*departure)/this.boxSize);
        this.lat = (la)->(int) floor(la/this.boxSize);
        this.invlon = (lo)->((lo+boxSize2)/departure)*boxSize;
        this.invlat = (la)->(la+boxSize2)*boxSize;
        this.squareCreator = squareCreator;
    }
    /**
     * Must be implemented if getOrCreate is used.
     * @param k1
     * @param k2
     * @return 
     */
    protected V itemCreator(double k1, double k2)
    {
        return squareCreator.apply(k1, k2);
    }
    
    @Override
    protected V itemCreator(Integer lo, Integer la)
    {
        return itemCreator(invLongitude(lo), invLatitude(la));
    }

    public V put(double longitude, double latitude, V value)
    {
        return super.put(lon.applyAsInt(longitude), lat.applyAsInt(latitude), value);
    }
    public V get(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude));
    }
    public V getOrCreate(double longitude, double latitude)
    {
        return super.getOrCreate(lon.applyAsInt(longitude), lat.applyAsInt(latitude));
    }
    public V getNorth(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude)+1);
    }
    public V getNorthEast(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude)+1);
    }
    public V getNorthWest(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude)+1);
    }
    public V getSouth(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude), lat.applyAsInt(latitude)-1);
    }
    public V getSouthEast(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude)-1);
    }
    public V getSouthWest(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude)-1);
    }
    public V getEast(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)+1, lat.applyAsInt(latitude));
    }
    public V getWest(double longitude, double latitude)
    {
        return super.get(lon.applyAsInt(longitude)-1, lat.applyAsInt(latitude));
    }
    
    public void forEachCoordinate(CoordinateConsumer<V> act)
    {
        super.forEach((lon,lat,v)->
        {
            act.accept(invlon.applyAsDouble(lon), invlat.applyAsDouble(lat), v);
        });
    }
    public V nearest(double longitude, double latitude)
    {
        if (map.isEmpty())
        {
            return null;
        }
        V res = get(longitude, latitude);
        if (res != null)
        {
            return res;
        }
        int loni = lon.applyAsInt(longitude);
        int lati = lat.applyAsInt(latitude);
        long min = Long.MAX_VALUE;
        int bestLon = 0;
        int bestLat = 0;
        Iterator<Integer> it = nearest(loni);
        while (it.hasNext())
        {
            Integer lonKey = it.next();
            if (distance(loni, lonKey) > min)
            {
                break;
            }
            NavigableMap<Integer, V> latMap = map.get(lonKey);
            Integer latKey = nearest(latMap, lati);
            long distance = distance(loni, lati, lonKey, latKey);
            if (distance < min)
            {
                min = distance;
                bestLon = lonKey;
                bestLat = latKey;
            }
        }
        return super.get(bestLon, bestLat);
    }
    private Integer nearest(NavigableMap<Integer, V> m, int k)
    {
        if (m.containsKey(k))
        {
            return k;
        }
        Integer ceilingKey = m.ceilingKey(k);
        Integer floorKey = m.floorKey(k);
        if (ceilingKey != null && floorKey != null)
        {
            return less(k, ceilingKey, floorKey);
        }
        else
        {
            if (ceilingKey != null)
            {
                return ceilingKey;
            }
            else
            {
                return floorKey;
            }
        }
    }
    private Iterator<Integer> nearest(int k)
    {
        NavigableMap<Integer, NavigableMap<Integer, V>> headMap = map.headMap(k, true);
        NavigableMap<Integer, NavigableMap<Integer, V>> tailMap = map.tailMap(k, false);
        return Merger.merge(
                (i,j)->abs(i-k)-abs(j-k), 
                headMap.descendingKeySet().iterator(),
                tailMap.keySet().iterator()
                );
    }
    private int less(int k, int i, int j)
    {
        if (abs(i-k) < abs(j-k))
        {
            return i;
        }
        else
        {
            return j;
        }
    }
    private long distance(long x1, long x2)
    {
        return sq(x2-x1);
    }
    private long distance(long x1, long y1, long x2, long y2)
    {
        return sq(x2-x1)+sq(y2-y1);
    }
    private long sq(long x)
    {
        return x*x;
    }
    private double invLongitude(int i)
    {
        return invlon.applyAsDouble(i);
    }
    private double invLatitude(int i)
    {
        return invlat.applyAsDouble(i);
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        forEachCoordinate((double lon, double lat, V v)->
        {
            sb.append('\n');
            sb.append(CoordinateFormat.format(lon, COORDINATE_DEGREES_AND_MINUTES_LONGITUDE));
            sb.append(", ");
            sb.append(CoordinateFormat.format(lat, COORDINATE_DEGREES_AND_MINUTES_LATITUDE));
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
