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
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;

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
    private BiConsumer<K,V> removeObserver;
    private final LongSupplier millis;
    /**
     * Creates TimeToLiveMap with system clock
     * @param defaultTimeout
     * @param unit 
     * @see java.time.Clock#systemUTC() 
     */
    public TimeToLiveMap(long defaultTimeout, TimeUnit unit)
    {
        this(defaultTimeout, unit, null);
    }
    /**
     * Creates TimeToLiveMap
     * @param defaultTimeout
     * @param unit
     * @param removeObserver is called when mapping has been removed.
     */
    public TimeToLiveMap(long defaultTimeout, TimeUnit unit, BiConsumer<K,V> removeObserver)
    {
        this(Clock.systemUTC(), defaultTimeout, unit, removeObserver);
    }
    /**
     * Creates TimeToLiveMap
     * @param clock
     * @param defaultTimeout
     * @param unit 
     */
    public TimeToLiveMap(Clock clock, long defaultTimeout, TimeUnit unit)
    {
        this(clock, defaultTimeout, unit, null);
    }
    /**
     * Creates TimeToLiveMap
     * @param clock
     * @param defaultTimeout
     * @param unit
     * @param removeObserver is called when mapping has been removed.
     */
    public TimeToLiveMap(Clock clock, long defaultTimeout, TimeUnit unit, BiConsumer<K,V> removeObserver)
    {
        this(clock::millis, defaultTimeout, unit, removeObserver);
    }
    /**
     * Creates TimeToLiveMap
     * @param millis
     * @param defaultTimeout
     * @param unit
     * @param removeObserver 
     */
    public TimeToLiveMap(LongSupplier millis, long defaultTimeout, TimeUnit unit, BiConsumer<K,V> removeObserver)
    {
        this.ttlSet = new TimeToLiveSet<>(millis, defaultTimeout, unit, this::onRemove);
        this.removeObserver = removeObserver;
        this.millis = millis;
    }
    private void onRemove(K key)
    {
        V value = map.remove(key);
        if (removeObserver != null)
        {
            removeObserver.accept(key, value);
        }
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
        if (removeObserver != null)
        {
            map.forEach(removeObserver);
        }
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
        if (removeObserver != null)
        {
            removeObserver.accept((K) key, old);
        }
        ttlSet.remove(key);
        map.remove(key);
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
        if (timeout != -1)
        {
            return put(key, value, millis.getAsLong() + unit.toMillis(timeout));
        }
        else
        {
            return put(key, value, -1);
        }
    }
    /**
     * Puts/refreshes mapping with given expiry time
     * @param key
     * @param value
     * @param expires
     * @return 
     */
    public V put(K key, V value, long expires)
    {
        if (lastPurge < millis.getAsLong())
        {
            size();
            lastPurge = millis.getAsLong() + purgeLimit;
        }
        V old = null;
        if (ttlSet.contains(key))
        {
            old = map.get(key);
        }
        map.put(key, value);
        if (expires == -1)
        {
            ttlSet.add(key);
        }
        else
        {
            ttlSet.add(key, expires);
        }
        return old;
    }
    /**
     * Returns mapped value. Mapping might be expired. If this is not desired
     * call contains before get.
     * @param key
     * @return 
     */
    @Override
    public V get(Object key)
    {
        V v = map.get(key);
        if (v != null)
        {
            ttlSet.add((K) key);
        }
        return v;
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
