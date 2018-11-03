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
package org.vesalainen.util;

import java.time.Clock;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * TimeToLiveMap is a ConcurrentHashMap backed implementation of Map interface
 * where each mapping has time-to-live. After ttl the mapping doesn't exist.
 * <p>
 * It is not possible to access expired mappings. However mappings are not 
 * actively removed by other than using iterators of this class or put method.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <V>
 * @see org.vesalainen.util.TimeToLiveSet
 */
public class TimeToLiveMap<K,V> extends AbstractMap<K,V>
{
    private static final long INIT_PURGE_LIMIT = 10000;
    private TimeToLiveSet<K> ttlSet;
    private Map<K,V> map = new ConcurrentHashMap<>();
    private long purgeLimit = INIT_PURGE_LIMIT;
    private long lastPurge;
    /**
     * Creates TimeToLiveMap with system clock
     * @param defaultTimeout
     * @param unit 
     * @see java.time.Clock#systemUTC() 
     */
    public TimeToLiveMap(long defaultTimeout, TimeUnit unit)
    {
        this(Clock.systemUTC(), defaultTimeout, unit);
    }
    /**
     * Creates TimeToLiveMap
     * @param clock
     * @param defaultTimeout
     * @param unit 
     */
    public TimeToLiveMap(Clock clock, long defaultTimeout, TimeUnit unit)
    {
        this.ttlSet = new TimeToLiveSet<>(clock, defaultTimeout, unit);
    }
    /**
     * Returns live-set of keys
     * @return 
     */
    @Override
    public Set<K> keySet()
    {
        return ttlSet;
    }

    @Override
    public void clear()
    {
        ttlSet.clear();
    }

    @Override
    public V remove(Object key)
    {
        V old = null;
        if (ttlSet.contains(key))
        {
            old = map.get(key);
        }
        ttlSet.remove(key);
        return old;
    }
    /**
     * Puts/refreshes mapping with default timeout
     * @param key
     * @param value
     * @return 
     */
    @Override
    public V put(K key, V value)
    {
        return put(key, value, -1, null);
    }
    /**
     * Puts/refreshes mapping with given timeout
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @return 
     */
    public V put(K key, V value, long timeout, TimeUnit unit)
    {
        if (lastPurge < System.currentTimeMillis())
        {
            size();
            lastPurge = System.currentTimeMillis() + purgeLimit;
        }
        V old = null;
        if (ttlSet.contains(key))
        {
            old = map.get(key);
        }
        map.put(key, value);
        if (unit == null)
        {
            ttlSet.add(key);
        }
        else
        {
            ttlSet.add(key, timeout, unit);
        }
        return old;
    }

    @Override
    public V get(Object key)
    {
        if (ttlSet.contains(key))
        {
            return map.get(key);
        }
        return null;
    }
    /**
     * Returns true if map contains key and it haven't expired.
     * @param key
     * @return 
     */
    @Override
    public boolean containsKey(Object key)
    {
        return ttlSet.contains(key);
    }

    @Override
    public boolean isEmpty()
    {
        return ttlSet.isEmpty();
    }
    /**
     * Returns current size of map. Note! This evaluates all mappings expiration.
     * @return 
     */
    @Override
    public int size()
    {
        return ttlSet.size();
    }
    /**
     * Returns a snapshot entry-set. This set's items expires only by calling
     * methods like size() which evaluate all mappings.
     * @return 
     */
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        size();
        return map.entrySet();
    }

    public long getPurgeLimit()
    {
        return purgeLimit;
    }
    /**
     * Set number of milliseconds after put will attempt to purge stale entries
     * after previous put.
     * @param millis 
     */
    public void setPurgeLimit(long millis)
    {
        this.purgeLimit = millis;
    }

    void setClock(Clock clock)
    {
        ttlSet.setClock(clock);
    }
    
}
