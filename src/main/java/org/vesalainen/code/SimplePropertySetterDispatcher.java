/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.code;

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapList;
import org.vesalainen.util.MapSet;

/**
 * SimplePropertySetterDispatcher is a PropertySetter which can dispatch property
 settings to several PropertySetters acting as observers.
 * @author tkv
 */
public class SimplePropertySetterDispatcher implements PropertySetterDispatcher
{
    private final MapSet<String,PropertySetter> mapSet;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReadLock readLock = rwLock.readLock();
    private final WriteLock writeLock = rwLock.writeLock();

    public SimplePropertySetterDispatcher()
    {
        this(new HashMapSet<>());
    }

    public SimplePropertySetterDispatcher(MapSet<String, PropertySetter> mapSet)
    {
        this.mapSet = mapSet;
    }
    
    @Override
    public String[] getPrefixes()
    {
        Set<String> keySet = mapSet.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public void addObserver(String key, PropertySetter ps)
    {
        writeLock.lock();
        try
        {
            mapSet.add(key, ps);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    @Override
    public void removeObserver(String key, PropertySetter ps)
    {
        writeLock.lock();
        try
        {
            mapSet.removeItem(key, ps);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    @Override
    public void set(String property, boolean arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, char arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, short arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, int arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, long arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, float arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, double arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        readLock.lock();
        try
        {
            mapSet.get(property).stream().forEach((ps) ->
            {
                ps.set(property, arg);
            });
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        return mapSet.isEmpty();
    }
    
    @Override
    public boolean containsProperty(String property)
    {
        return mapSet.containsKey(property);
    }
}
