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
    private LongMap<T> map = new LongMap<>(new ConcurrentHashMap<>());
    private LongSupplier millis;
    private long defaultTimeout;
    private Consumer<T> onRemove;
    /**
     * Creates a new TimeToLiveSet using UTC clock
     * @param defaultTimeout
     * @param unit 
     * @see java.time.Clock#systemUTC() 
     */
    public TimeToLiveSet(long defaultTimeout, TimeUnit unit)
    {
        this(Clock.systemUTC(), defaultTimeout, unit);
    }
    /**
     * Creates a new TimeToLiveSet
     * @param clock
     * @param defaultTimeout
     * @param unit 
     */
    public TimeToLiveSet(Clock clock, long defaultTimeout, TimeUnit unit)
    {
        this(clock, defaultTimeout, unit, (k)->{});
    }
    /**
     * Creates a new TimeToLiveSet
     * @param clock
     * @param defaultTimeout
     * @param unit
     * @param onRemove IS called when item is removed
     */
    public TimeToLiveSet(Clock clock, long defaultTimeout, TimeUnit unit, Consumer<T> onRemove)
    {
        this(clock::millis, defaultTimeout, unit, onRemove);
    }
    public TimeToLiveSet(LongSupplier millis, long defaultTimeout, TimeUnit unit, Consumer<T> onRemove)
    {
        this.millis = millis;
        this.defaultTimeout = unit.toMillis(defaultTimeout);
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
        return add(item, defaultTimeout, TimeUnit.MILLISECONDS);
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
     * @param timeout
     * @param unit 
     * @return  
     */
    public boolean add(T item, long timeout, TimeUnit unit)
    {
        return add(item, millis.getAsLong() + unit.toMillis(timeout));
    }
    /**
     * Add/Refresh item with given expiry time
     * @param item
     * @param expires
     * @return 
     */
    public boolean add(T item, long expires)
    {
        LongReference old = map.put(item, expires);
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
        LongReference old = map.remove((T)key);
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
     * Return true if item has not expired
     * @param o
     * @return 
     */
    @Override
    public boolean contains(Object o)
    {
        T item = (T) o;
        try
        {
            long expires = map.getLong(item);
            if (expires < millis.getAsLong())
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
        catch (IllegalArgumentException ex)
        {
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
        private Iterator<Entry<T, LongReference>> iterator = map.entrySet().iterator();
        private Entry<T, LongReference> entry;

        @Override
        public boolean hasNext()
        {
            while (iterator.hasNext())
            {
                entry = iterator.next();
                if (entry.getValue().value >= millis.getAsLong())
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
                Entry<T, LongReference> e = entry;
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
}
