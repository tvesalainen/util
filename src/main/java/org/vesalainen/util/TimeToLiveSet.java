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
package org.vesalainen.util;

import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A set for items that become stale after timeout
 * @author tkv
 * @param <T>
 */
public class TimeToLiveSet<T> implements Iterable<T>
{
    private LongMap<T> map = new LongMap<>();
    private Clock clock;
    private long defaultTimeout;
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
        this.clock = clock;
        this.defaultTimeout = unit.toMillis(defaultTimeout);
    }
    /**
     * Just for testing
     * @param clock 
     */
    void setClock(Clock clock)
    {
        this.clock = clock;
    }
    
    /**
     * Refresh item with default timeout
     * @param item 
     */
    public void refresh(T item)
    {
        refresh(item, defaultTimeout, TimeUnit.MILLISECONDS);
    }
    /**
     * 
     * @param item
     * @param defaultTimeout
     * @param unit 
     */
    public void refresh(T item, long defaultTimeout, TimeUnit unit)
    {
        map.put(item, clock.millis() + unit.toMillis(defaultTimeout));
    }
    /**
     * Return true if item has not expired
     * @param item
     * @return 
     */
    public boolean isAlive(T item)
    {
        try
        {
            long expires = map.getLong(item);
            if (expires < clock.millis())
            {
                map.remove(item);
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
     * Return true is there are no refresh items.
     * <p>Note that this method goes through all items
     * @return 
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }
    /**
     * Returns current count of refresh items.
     * <p>Note that this method goes through all items
     * @return 
     */
    public long size()
    {
        return stream().count();
    }
    /**
     * Return stream of refresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    public Stream<T> stream()
    {
        return StreamSupport.stream(new SpliteratorImpl(), false);
    }
    /**
     * Returns stream of refresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    @Override
    public Spliterator<T> spliterator()
    {
        return new SpliteratorImpl();
    }
    /**
     * Return iterator of refresh items
     * <p>Items can become stale while processing.
     * @return 
     */
    @Override
    public Iterator<T> iterator()
    {
        return Spliterators.iterator(new SpliteratorImpl());
    }
    private class SpliteratorImpl implements Spliterator<T>
    {
        private Iterator<Entry<T,Long>> iterator = map.entrySet().iterator();
        
        @Override
        public boolean tryAdvance(Consumer<? super T> action)
        {
            while (iterator.hasNext())
            {
                Entry<T, Long> entry = iterator.next();
                if (entry.getValue() >= clock.millis())
                {
                    action.accept(entry.getKey());
                    return true;
                }
                iterator.remove();
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return map.size();
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
}
