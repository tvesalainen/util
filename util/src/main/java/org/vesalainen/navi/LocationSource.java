/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class LocationSource implements LocationObserver
{
    private static final Map<Object,LocationSource> sources = new HashMap<>();
    private static LocationSource current;
    private static final List<LocationObserver> observers = new ArrayList<>();
    
    public static void register(Object key, LocationSource source)
    {
        sources.put(key, source);
    }
    public static void unregister(Object key)
    {
        sources.remove(key);
    }
    public static void addObserver(LocationObserver observer)
    {
        observers.add(observer);
    }
    public static void removeObserver(LocationObserver observer)
    {
        observers.remove(observer);
    }
    public static void activate(Object key) throws Exception
    {
        LocationSource source = sources.get(key);
        if (source == null)
        {
            throw new IllegalArgumentException("source for key "+key+" not found");
        }
        if (current != null)
        {
            current.stop();
        }
        current = source;
        current.start();
    }
    public static void deactivate() throws Exception
    {
        if (current != null)
        {
            current.stop();
            current = null;
        }
    }
    protected abstract void start() throws Exception;
    protected abstract void stop() throws Exception;

    @Override
    public void update(double longitude, double latitude, long time)
    {
        for (LocationObserver observer : observers)
        {
            observer.update(longitude, latitude, time);
        }
    }

    @Override
    public void update(double longitude, double latitude, long time, double accuracy)
    {
        for (LocationObserver observer : observers)
        {
            observer.update(longitude, latitude, time, accuracy);
        }
    }

    @Override
    public void update(double longitude, double latitude, long time, double accuracy, double speed)
    {
        for (LocationObserver observer : observers)
        {
            observer.update(longitude, latitude, time, accuracy, speed);
        }
    }

    @Override
    public void reset()
    {
        for (LocationObserver observer : observers)
        {
            observer.reset();
        }
    }
    
}
