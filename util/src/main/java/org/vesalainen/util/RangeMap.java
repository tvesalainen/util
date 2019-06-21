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
import org.vesalainen.util.navi.AbstractLocationSupport.SerializableComparator;

/**
 * RangeMap is a non unique mapping from range to item
 * <p>This class thread-safe except overlapping methods which is not necessarily
 * snapshot.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeMap<K,V> implements Serializable
{
    protected static final long serialVersionUID = 1L;

    protected final SerializableComparator<K> comparator;
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    private List<Entry> fromList = new ArrayList<>();
    private List<Entry> toList = new ArrayList<>();
    private boolean sorted;

    protected RangeMap()
    {
        this(null);
    }

    public RangeMap(SerializableComparator<K> comparator)
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
        put(new SimpleRange<>(from, to, comparator), value);
    }
    /**
     * Puts range.
     * @param range
     * @param value 
     */
    protected void put(Range<K> range, V value)
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
        return overlappingEntries(from, to).map((e)->e.value);
    }
    protected Stream<Entry>  overlappingEntries(K from, K to)
    {
        return overlappingEntries(new SimpleRange<>(from, to, comparator));
    }
    /**
     * Returns stream of values whose range overlaps given range.
     * @param range
     * @return 
     */
    public Stream<V>  overlapping(Range<K> range)
    {
        return overlappingEntries(range).map((e)->e.value);
    }
    protected Stream<Entry>  overlappingEntries(Range<K> range)
    {
        if (!range.isInOrder())
        {
            throw new IllegalArgumentException("from > to");
        }
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
            int size = toList.size();
            int excludeFrom = excludeFrom(p1);
            int excludeTo = excludeTo(p2);
            int hits = size - excludeFrom - excludeTo;
            int max = size - 1;
            int ip1 = Math.min(insertPoint(p1), max);
            int ip2 = Math.min(insertPoint(p2), max);
            if (max - ip2 < ip1)
            {
                return StreamSupport.stream(new IntersectingSpliterator(toList, range, hits, ip2, 1), false);
            }
            else
            {
                return StreamSupport.stream(new IntersectingSpliterator(fromList, range, hits, ip1, -1), false);
            }
        }
        finally
        {
            readLock.unlock();
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
        int idx = search(list, key);
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
        int idx = search(list, key);
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
    private int search(List<Entry> list, Entry key)
    {
        Range<K> range = key.range;
        if (range.getFrom() == null)
        {
            return 0;
        }
        else
        {
            if (range.getTo() == null)
            {
                return list.size();
            }
            else
            {
                return Collections.binarySearch(list, key);
            }
        }
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
        if (idx >= 0)
        {
            return idx;
        }
        else
        {
            return -idx-1;
        }
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
    private class IntersectingSpliterator extends AbstractSpliterator<Entry>
    {
        List<Entry> list;
        private Range<K> range;
        private int hits;
        private int index;
        private int step;

        public IntersectingSpliterator(List<Entry> list, Range<K> range, int hits, int index, int step)
        {
            super(hits, 0);
            this.list = list;
            this.range = range;
            this.hits = hits;
            this.index = index;
            this.step = step;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry> action)
        {
            if (hits == 0)
            {
                return false;
            }
            Entry entry = list.get(index);
            while (!range.isOverlapping(entry.range))
            {
                index += step;
                entry = list.get(index);
            }
            index += step;
            hits--;
            action.accept(entry);
            return true;
        }

    }
    public class Entry implements Comparable<Entry>, Serializable
    {
        protected static final long serialVersionUID = 1L;
        private K key;
        private Range<K> range;
        private V value;

        public Entry(K key, Range<K> range, V value)
        {
            this.key = key;
            this.range = range;
            this.value = value;
        }

        public K getKey()
        {
            return key;
        }

        public Range<K> getRange()
        {
            return range;
        }

        public V getValue()
        {
            return value;
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
