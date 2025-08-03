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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * RingIterator iterates all NavigableMap entries ascending starting from key and
 * ending entry before key.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingIterator<K,V> implements Iterator<Entry<K,V>>
{
    private Iterator<Entry<K,V>> head;
    private Iterator<Entry<K,V>> tail;
    /**
     * Creates a RingIterator
     * @param key
     * @param map 
     */
    public RingIterator(K key, NavigableMap<K,V> map)
    {
        tail = map.tailMap(key, true).entrySet().iterator();
        head = map.headMap(key, false).entrySet().iterator();
    }
    /**
     * Returns a stream that iterates all NavigableMap entries ascending starting from key and
     * ending entry before key.
     * @param <K>
     * @param <V>
     * @param key
     * @param map
     * @param parallel
     * @return 
     */
    public static final <K,V> Stream<Entry<K,V>> stream(K key, NavigableMap<K,V> map, boolean parallel)
    {
        return StreamSupport.stream(spliterator(key, map), parallel);
    }
    /**
     * Returns a spliterator that iterates all NavigableMap entries ascending starting from key and
     * ending entry before key.
     * @param <K>
     * @param <V>
     * @param key
     * @param map
     * @return 
     */
    public static final <K,V> Spliterator<Entry<K,V>> spliterator(K key, NavigableMap<K,V> map)
    {
        return Spliterators.spliterator(new RingIterator(key, map), map.size(), 0);
    }
    @Override
    public boolean hasNext()
    {
        return tail.hasNext() || head.hasNext();
    }

    @Override
    public Entry<K, V> next()
    {
        if (tail.hasNext())
        {
            return tail.next();
        }
        else
        {
            return head.next();
        }
    }
    
}
