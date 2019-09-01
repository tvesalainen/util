/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A set for items that become stale after timeout
 * <p>This set is backed up by ConcurrentHashMap
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class TimeToLiveSet<T> extends AbstractSet<T>
{
    private Map<T,TTLEntry> map = new ConcurrentHashMap<>();
    private LongSupplier millis;
    private long defaultTTL;
    private Consumer<T> onRemove;
    /**
     * Creates a new TimeToLiveSet using UTC clock
     * @param defaultTTL
     * @param unit 
     * @see java.time.Clock#systemUTC() 
     */
    public TimeToLiveSet(long defaultTTL, TimeUnit unit)
    {
        this(Clock.systemUTC(), defaultTTL, unit);
    }
    /**
     * Creates a new TimeToLiveSet
     * @param clock
     * @param defaultTTL
     * @param unit 
     */
    public TimeToLiveSet(Clock clock, long defaultTTL, TimeUnit unit)
    {
        this(clock, defaultTTL, unit, (k)->{});
    }
    /**
     * Creates a new TimeToLiveSet
     * @param clock
     * @param defaultTTL
     * @param unit
     * @param onRemove IS called when item is removed
     */
    public TimeToLiveSet(Clock clock, long defaultTTL, TimeUnit unit, Consumer<T> onRemove)
    {
        this(clock::millis, defaultTTL, unit, onRemove);
    }
    public TimeToLiveSet(LongSupplier millis, long defaultTTL, TimeUnit unit, Consumer<T> onRemove)
    {
        this.millis = millis;
        this.defaultTTL = unit.toMillis(defaultTTL);
        this.onRemove = onRemove;
    }
    /**
     * Just for testing
     * @param clock 
     */
    void setClock(Clock clock)
    {
        this.millis = clock::millis;
    }
    /**
     * @deprecated Use add
     * @param item 
     */
    public void refresh(T item)
    {
        add(item);
    }
    /**
     * Add/Refresh item with default timeout
     * @param item 
     * @return  
     */
    @Override
    public boolean add(T item)
    {
        TTLEntry old = map.get(item);
        if (old != null)
        {
            old.refresh();
        }
        else
        {
            add(item, defaultTTL, TimeUnit.MILLISECONDS);
        }
        return old != null;
    }
    /**
     * @deprecated Use add
     * @param item
     * @param timeout
     * @param unit 
     */
    public void refresh(T item, long timeout, TimeUnit unit)
    {
        add(item, timeout, unit);
    }
    /**
     * Add/Refresh item with given timeout
     * @param item
     * @param ttl
     * @param unit 
     * @return  
     */
    public boolean add(T item, long ttl, TimeUnit unit)
    {
        return add(item, ttl, millis.getAsLong() + unit.toMillis(ttl));
    }
    /**
     * Add/Refresh item with given expiry time
     * @param item
     * @param expires
     * @return 
     */
    public boolean add(T item, long ttl, long expires)
    {
        TTLEntry old = map.get(item);
        if (old != null)
        {
            old.refresh(ttl, expires);
        }
        else
        {
            map.put(item, new TTLEntry(ttl, expires));
        }
        return old != null;
    }
    @Override
    public void clear()
    {
        for (T item : map.keySet())
        {
            onRemove.accept(item);
        }
        map.clear();
    }

    @Override
    public boolean remove(Object key)
    {
        TTLEntry old = map.remove((T)key);
        onRemove.accept((T) key);
        return old != null;
    }
    /**
     * @deprecated Use contains
     * @param item
     * @return 
     */
    public boolean isAlive(T item)
    {
        return contains(item);
    }
    /**
     * Return true if set contains item and it has not expired
     * @param o
     * @return 
     */
    @Override
    public boolean contains(Object o)
    {
        T item = (T) o;
        TTLEntry old = map.get(item);
        if (old != null)
        {
            if (old.expires < millis.getAsLong())
            {
                map.remove(item);
                onRemove.accept(item);
                return false;
            }
            else
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Return true is there are no fresh items.
     * <p>Note that this method goes through all items
     * @return 
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }
    /**
     * Returns current count of fresh items.
     * <p>Note that this method goes through all items
     * @return 
     */
    @Override
    public int size()
    {
        int count = 0;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            count++;
        }
        return count;
    }

    /**
     * Return stream of fresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    @Override
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }
    /**
     * Returns stream of fresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    @Override
    public Spliterator<T> spliterator()
    {
        return Spliterators.spliterator(iterator(), map.size(), 0);
    }
    /**
     * Return iterator of fresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    @Override
    public Iterator<T> iterator()
    {
        return new IteratorImpl();
    }
    private class IteratorImpl implements Iterator<T>
    {
        private Iterator<Entry<T, TTLEntry>> iterator = map.entrySet().iterator();
        private Entry<T, TTLEntry> entry;

        @Override
        public boolean hasNext()
        {
            while (iterator.hasNext())
            {
                entry = iterator.next();
                if (entry.getValue().expires >= millis.getAsLong())
                {
                    return true;
                }
                iterator.remove();
                onRemove.accept(entry.getKey());
            }
            entry = null;
            return false;
        }

        @Override
        public T next()
        {
            if (entry != null)
            {
                Entry<T, TTLEntry> e = entry;
                entry = null;
                return e.getKey();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            iterator.remove();
        }
        
    }
    private class TTLEntry
    {
        private long ttl;
        private long expires;

        private TTLEntry(long ttl, long expires)
        {
            this.ttl = ttl;
            this.expires = expires;
        }
        private void refresh(long ttl, long expires)
        {
            this.ttl = ttl;
            this.expires = expires;
        }
        private void refresh()
        {
            expires = millis.getAsLong() + ttl;
        }
    }
}
