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
package org.vesalainen.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * RangeDB is a non unique mapping from range to item
 * <p>This class thread-safe except overlapping methods which is not necessarily
 * snapshot.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeDB<K,V> implements Serializable
{
    protected static final long serialVersionUID = 1L;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    private Comparator<K> comparator;
    private List<Entry> fromList = new ArrayList<>();
    private List<Entry> toList = new ArrayList<>();
    private boolean sorted;

    public RangeDB()
    {
        this(null);
    }

    public RangeDB(Comparator<K> comparator)
    {
        this.comparator = comparator;
    }
    
    /**
     * Puts range.
     * @param from
     * @param to
     * @param value 
     */
    public void put(K from, K to, V value)
    {
        put(new SimpleRange<>(from, to), value);
    }
    /**
     * Puts range.
     * @param range
     * @param value 
     */
    public void put(Range<K> range, V value)
    {
        writeLock.lock();
        try
        {
            fromList.add(new Entry(range.getFrom(), range, value));
            toList.add(new Entry(range.getTo(), range, value));
            sorted = false;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    /**
     * Returns stream of values whose range overlaps given range.
     * @param from
     * @param to
     * @return 
     */
    public Stream<V>  overlapping(K from, K to)
    {
        return overlapping(new SimpleRange<>(from, to));
    }
    /**
     * Returns stream of values whose range overlaps given range.
     * @param range
     * @return 
     */
    public Stream<V>  overlapping(Range<K> range)
    {
        if (toList.isEmpty())
        {
            return Stream.empty();
        }
        ensureSorted();
        readLock.lock();
        try
        {
            Entry fromKey = new Entry(range.getFrom(), range, null);
            Entry toKey = new Entry(range.getTo(), range, null);
            int p1 = point(fromList, toKey, false);
            int p2 = point(toList, fromKey, true);
            int hits = toList.size() - excludeFrom(p1) - excludeTo(p2);
            int ip = insertPoint(p2);
            return StreamSupport.stream(new IntersectingSpliterator(toList, range, hits, ip), false).map((e)->e.value);
        }
        finally
        {
            readLock.unlock();
        }
    }
    private class IntersectingSpliterator extends AbstractSpliterator<Entry>
    {
        List<Entry> list;
        private Range<K> range;
        private int hits;
        private int index;

        public IntersectingSpliterator(List<Entry> list, Range<K> range, int hits, int index)
        {
            super(hits, 0);
            this.list = list;
            this.range = range;
            this.hits = hits;
            this.index = index;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry> action)
        {
            if (hits == 0)
            {
                return false;
            }
            Entry entry = toList.get(index);
            while (!range.isOverlapping(entry.range))
            {
                index++;
                entry = toList.get(index);
            }
            index++;
            hits--;
            action.accept(entry);
            return true;
        }

    }
    private int point(List<Entry> list, Entry key, boolean up)
    {
        if (up)
        {
            return highPoint(list, key);
        }
        else
        {
            return lowPoint(list, key);
        }
    }
    private int lowPoint(List<Entry> list, Entry key)
    {
        int idx = Collections.binarySearch(list, key);
        if (idx >= 0)
        {
            while (idx > 1 && key.compareTo(list.get(idx-1)) == 0)
            {
                idx--;
            }
        }
        return idx;
    }
    private int highPoint(List<Entry> list, Entry key)
    {
        int idx = Collections.binarySearch(list, key);
        if (idx >= 0)
        {
            int size = list.size() - 2;
            while (idx < size && key.compareTo(list.get(idx+1)) == 0)
            {
                idx++;
            }
        }
        return idx;
    }
    int excludeFrom(int idx)
    {
        if (idx >= 0)
        {
            return fromList.size() - idx;
        }
        else
        {
            return fromList.size() - insertPoint(idx);
        }
    }
    int excludeTo(int idx)
    {
        if (idx >= 0)
        {
            return idx+1;
        }
        else
        {
            return insertPoint(idx);
        }
    }
    private int insertPoint(int idx)
    {
        return -idx-1;
    }
    void ensureSorted()
    {
        writeLock.lock();
        try
        {
            if (!sorted)
            {
                fromList.sort(null);
                toList.sort(null);
                sorted = true;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    private class Entry implements Comparable<Entry>
    {
        private K key;
        private Range<K> range;
        private V value;

        public Entry(K key, Range<K> range, V value)
        {
            this.key = key;
            this.range = range;
            this.value = value;
        }
        
        @Override
        public int compareTo(Entry o)
        {
            return CollectionHelp.compare(key, o.key, comparator);
        }

        @Override
        public String toString()
        {
            return "Entry{" + "key=" + key + ", range=" + range + ", value=" + value + '}';
        }
        
    }
}
