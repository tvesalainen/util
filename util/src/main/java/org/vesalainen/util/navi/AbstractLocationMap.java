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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Stream;
import org.vesalainen.util.navi.AbstractLocationSupport.CoordinateSupplier;

/**
 * AbstractLocationMap is a base class for location related data store. Location
 * mappings are not unique. One location can be associated with several data entries.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractLocationMap<L,T> implements Comparator<Entry<L,T>>, Serializable
{
    protected static final long serialVersionUID = 1L;

    private CoordinateSupplier<L> longitudeSupplier;
    private CoordinateSupplier<L> latitudeSupplier;
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReadLock readLock = rwLock.readLock();
    private WriteLock writeLock = rwLock.writeLock();
    private List<Entry<L,T>> entries = new ArrayList<>();
    private boolean sorted;

    protected AbstractLocationMap()
    {
    }

    protected AbstractLocationMap(AbstractLocationSupport support)
    {
        this.longitudeSupplier = support.longitudeSupplier;
        this.latitudeSupplier = support.latitudeSupplier;
    }
    
    public void put(L location, T item)
    {
        writeLock.lock();
        try
        {
            entries.add(new SimpleEntry<>(location, item));
            sorted = false;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    private Stream<Entry<L, T>>  subStream(L southWest, boolean included1, L northEast, boolean included2)
    {
        ensureSorted();
        readLock.lock();
        try
        {
            int begin = point(southWest, !included1);
            int end = point(northEast, included2);
            return entries.subList(begin, end+1).stream();
        }
        finally
        {
            readLock.unlock();
        }
    }

    private Stream<Entry<L, T>>  tailStream(L southWest, boolean included)
    {
        ensureSorted();
        readLock.lock();
        try
        {
            int begin = point(southWest, !included);
            return entries.subList(begin, entries.size()).stream();
        }
        finally
        {
            readLock.unlock();
        }
    }

    private Stream<Entry<L, T>>  headStream(L northEast, boolean included)
    {
        ensureSorted();
        readLock.lock();
        try
        {
            int end = point(northEast, included);
            return entries.subList(0, end+1).stream();
        }
        finally
        {
            readLock.unlock();
        }
    }
    private void ensureSorted()
    {
        writeLock.lock();
        try
        {
            if (!sorted)
            {
                entries.sort(this);
                sorted = true;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    private int point(L location, boolean up)
    {
        if (up)
        {
            return highPoint(location);
        }
        else
        {
            return lowPoint(location);
        }
    }
    private int lowPoint(L location)
    {
        int ip = insertPoint(location);
        while (ip > 1 && location.equals(entries.get(ip-1).getKey()))
        {
            ip--;
        }
        return ip;
    }
    private int highPoint(L location)
    {
        int ip = insertPoint(location);
        int size = entries.size() - 2;
        while (ip < size && location.equals(entries.get(ip+1).getKey()))
        {
            ip++;
        }
        return ip;
    }
    private int insertPoint(L location)
    {
        Entry<L, T> key = new SimpleEntry<>(location, null);
        return insertPoint(Collections.binarySearch(entries, key, this));
    }
    private int insertPoint(int idx)
    {
        if (idx >= 0)
        {
            return idx;
        }
        else
        {
            return -idx-1;
        }
    }
    public Stream<T> strickValues(BoundingBox<L> bb)
    {
        return strickValues(bb.getSouthWest(), bb.getNorthEast());
    }
    public Stream<T> strickValues(L southWest, L northEast)
    {
        return strickEntries(southWest, northEast).map((e)->e.getValue());
    }
    public Stream<Entry<L, T>> strickEntries(BoundingBox<L> bb)
    {
        return strickEntries(bb.getSouthWest(), bb.getNorthEast());
    }
    public Stream<Entry<L, T>> strickEntries(L southWest, L northEast)
    {
        double south = latitudeSupplier.applyAsDouble(southWest);
        double north = latitudeSupplier.applyAsDouble(northEast);
        if (south > north)
        {
            throw new IllegalArgumentException("south > north");
        }
        Stream<Entry<L, T>> stream;
        if (longitudeSupplier.applyAsDouble(southWest) <= longitudeSupplier.applyAsDouble(northEast))
        {
            stream = subStream(southWest, false, northEast, true);
        }
        else
        {
            Stream<Entry<L, T>> stream1 = tailStream(southWest, true);
            Stream<Entry<L, T>> stream2 = headStream(northEast, true);
            stream = Stream.concat(stream1, stream2);
        }
        return stream.filter((e)->
        {
            double lat = latitudeSupplier.applyAsDouble(e.getKey());
            return (lat >= south && lat <= north);
        });
    }
    @Override
    public int compare(Entry<L, T> o1, Entry<L, T> o2)
    {
        int c = Double.compare(longitudeSupplier.applyAsDouble(o1.getKey()), longitudeSupplier.applyAsDouble(o2.getKey()));
        if (c == 0)
        {
            return Double.compare(latitudeSupplier.applyAsDouble(o1.getKey()), latitudeSupplier.applyAsDouble(o2.getKey()));
        }
        else
        {
            return c;
        }
    }

}
