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
package org.vesalainen.math.sliding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.code.PropertySetterDispatcher;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author tkv
 */
public class TimeoutStatsService implements PropertySetter
{
    private final PropertySetterDispatcher dispatcher;
    private final Map<String,Map<Integer,TimeoutStats>> map = new HashMap<>();
    private final MapList<TimeArray,StatsObserver> observerMap = new HashMapList<>();
    private Preferences preferences;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public TimeoutStatsService(PropertySetterDispatcher dispatcher)
    {
        this(dispatcher, null);
    }

    public TimeoutStatsService(PropertySetterDispatcher dispatcher, String preferencePath)
    {
        this.dispatcher = dispatcher;
        if (preferencePath != null)
        {
            try {
                Preferences userNodeForPackage = Preferences.userNodeForPackage(TimeoutStatsService.class);
                this.preferences = userNodeForPackage.node(preferencePath);
                for (String compositeProp : preferences.keys())
                {
                    boolean isAngle = preferences.getBoolean(compositeProp, false);
                    addObserver(compositeProp, null, isAngle);
                }
            }
            catch (BackingStoreException ex) 
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    
    public void addObserver(String compositeProp, StatsObserver observer)
    {
        addObserver(compositeProp, observer, false);
    }
    public void addObserver(String compositeProp, StatsObserver observer, boolean isAngle)
    {
        writeLock.lock();
        try
        {
            String[] sp = compositeProp.split(":");
            if (sp.length < 2)
            {
                throw new IllegalArgumentException(compositeProp+" not starting with <propertyname> ':' <seconds>");
            }
            String prop = sp[0];
            Map<Integer, TimeoutStats> statsMap = map.get(prop);
            if (statsMap == null)
            {
                statsMap = new HashMap<>();
                map.put(prop, statsMap);
                dispatcher.addObserver(prop, this);
            }
            int seconds = Integer.parseUnsignedInt(sp[1]);
            TimeoutStats ts = statsMap.get(seconds);
            if (ts == null)
            {
                if (isAngle)
                {
                    ts = new TimeoutSlidingAngleStats(seconds, seconds*1000);
                }
                else
                {
                    ts = new TimeoutSlidingStats(seconds, seconds*1000);
                }
                statsMap.put(seconds, ts);
            }
            if (observer != null)
            {
                observerMap.add(ts, observer);
                if (preferences != null)
                {
                    preferences.putBoolean(compositeProp, isAngle);
                }
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    public void removeObserver(String compositeProp, StatsObserver observer)
    {
        writeLock.lock();
        try
        {
            String[] sp = compositeProp.split(":");
            if (sp.length < 2)
            {
                throw new IllegalArgumentException(compositeProp+" not starting with <propertyname> ':' <seconds>");
            }
            String prop = sp[0];
            Map<Integer, TimeoutStats> statsMap = map.get(prop);
            if (statsMap != null)
            {
                int seconds = Integer.parseUnsignedInt(sp[1]);
                TimeArray ts = statsMap.get(seconds);
                if (ts != null)
                {
                    observerMap.removeItem(ts, observer);
                    if (preferences == null)
                    {
                        List<StatsObserver> list = observerMap.get(ts);
                        if (list.isEmpty())
                        {
                            observerMap.remove(ts);
                            statsMap.remove(seconds);
                            if (statsMap.isEmpty())
                            {
                                map.remove(prop);
                                dispatcher.removeObserver(prop, this);
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    private void fire(String property, double value)
    {
        readLock.lock();
        try
        {
            Map<Integer, TimeoutStats> statsMap = map.get(property);
            if (statsMap != null)
            {
                statsMap.values().stream().forEach((ts) ->
                {
                    ts.accept(value);
                    observerMap.get(ts).stream().forEach((so) ->
                    {
                        so.changed(ts);
                    });
                });
            }
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    @Override
    public String[] getPrefixes()
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void set(String property, boolean arg)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void set(String property, byte arg)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void set(String property, char arg)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void set(String property, short arg)
    {
        fire(property, arg);
    }

    @Override
    public void set(String property, int arg)
    {
        fire(property, arg);
    }

    @Override
    public void set(String property, long arg)
    {
        fire(property, arg);
    }

    @Override
    public void set(String property, float arg)
    {
        fire(property, arg);
    }

    @Override
    public void set(String property, double arg)
    {
        fire(property, arg);
    }

    @Override
    public void set(String property, Object arg)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    public interface StatsObserver
    {
        void changed(TimeArray stats);
    }
}
