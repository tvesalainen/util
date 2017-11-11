/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiPredicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <V>
 */
public class BinaryMap<K,V> extends AbstractMap<K,V> implements NavigableMap<K,V>
{
    private NavigableSet<Entry<K, V>> entrySet;
    private Comparator<? super K> comparator;

    public BinaryMap()
    {
        this.entrySet = new BinarySet<>();
    }

    public BinaryMap(Comparator<? super K> comparator)
    {
        this.comparator = comparator;
        this.entrySet = new BinarySet<>(Entry.comparingByKey(comparator));
    }
    
    public BinaryMap(Map<K,V> map)
    {
        this.entrySet = new BinarySet<>(map.entrySet());
    }
    public BinaryMap(SortedMap<K,V> map)
    {
        this.comparator = map.comparator();
        this.entrySet = new BinarySet<>(map.entrySet());
    }
    private BinaryMap(NavigableSet<Entry<K,V>> set, Comparator<? super K> keyComparator)
    {
        this.entrySet = set;
    }

    /**
     * {@inheritDoc}
     * @param key
     * @param value
     * @return 
     */
    @Override
    public V put(K key, V value)
    {
        BinarySet<Entry<K,V>> es = (BinarySet<Entry<K,V>>) entrySet;
        Entry<K,V> oldEntry = es.addEntry(entry(key, value));
        if (oldEntry != null)
        {
            return oldEntry.getValue();
        }
        return null;
    }
    private Entry<K,V> entry(K key, V value)
    {
        if (comparator == null)
        {
            Comparable<K> c = (Comparable) key;
            return new NaturalImpl(c, value);
        }
        else
        {
            Objects.requireNonNull(comparator, "comparator missing from non Comparable key");
            return new EntryImpl(Entry.comparingByKey(comparator), key, value);
        }
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public V get(Object key)
    {
        BinarySet<Entry<K,V>> es = (BinarySet<Entry<K,V>>) entrySet;
        Entry<K, V> entry = entry((K) key, null);
        Entry<K, V> res = es.get(entry);
        if (res != null)
        {
            return res.getValue();
        }
        return null;
    }
    /**
     * If an entry is found in binary-search, the predicate is tested and either
     * entry or null is result.
     * <p>
     * Otherwise entry before insertion-point is tested and if passed 
     * returned.
     * <p>
     * Otherwise entry at insert-point is tested and if passed returned.
     * <p>
     * If there is no entry at insert-point or preceding it returns null.
     * @param key
     * @param predicate
     * @return 
     */
    public Entry<K,V> get(K key, BiPredicate<K,V> predicate)
    {
        BinarySet<Entry<K,V>> es = (BinarySet<Entry<K,V>>) entrySet;
        Entry<K, V> entry = entry((K) key, null);
        return es.get(entry, (e)->predicate.test(e.getKey(), e.getValue()));
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public Entry<K, V> lowerEntry(K key)
    {
        Entry<K, V> entry = entry(key, null);
        return entrySet.lower(entry);
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public K lowerKey(K key)
    {
        Entry<K, V> entry = entry(key, null);
        Entry<K, V> lower = entrySet.lower(entry);
        if (lower != null)
        {
            return lower.getKey();
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public Entry<K, V> floorEntry(K key)
    {
        Entry<K, V> entry = entry(key, null);
        return entrySet.floor(entry);
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public K floorKey(K key)
    {
        Entry<K, V> entry = entry(key, null);
        Entry<K, V> floor = entrySet.floor(entry);
        if (floor != null)
        {
            return floor.getKey();
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public Entry<K, V> ceilingEntry(K key)
    {
        Entry<K, V> entry = entry(key, null);
        return entrySet.ceiling(entry);
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public K ceilingKey(K key)
    {
        Entry<K, V> entry = entry(key, null);
        Entry<K, V> ceiling = entrySet.ceiling(entry);
        if (ceiling != null)
        {
            return ceiling.getKey();
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public Entry<K, V> higherEntry(K key)
    {
        Entry<K, V> entry = entry(key, null);
        return entrySet.higher(entry);
    }
    /**
     * {@inheritDoc}
     * @param key
     * @return 
     */
    @Override
    public K higherKey(K key)
    {
        Entry<K, V> entry = entry(key, null);
        Entry<K, V> higher = entrySet.higher(entry);
        if (higher != null)
        {
            return higher.getKey();
        }
        return null;
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Entry<K, V> firstEntry()
    {
        return entrySet.first();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Entry<K, V> lastEntry()
    {
        return entrySet.last();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Entry<K, V> pollFirstEntry()
    {
        return entrySet.pollFirst();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public Entry<K, V> pollLastEntry()
    {
        return entrySet.pollLast();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public NavigableMap<K, V> descendingMap()
    {
        return new BinaryMap<>(entrySet.descendingSet(), comparator);
    }
    /**
     * {@inheritDoc}
     * Not supported yet!
     * @return 
     */
    @Override
    public NavigableSet<K> navigableKeySet()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * {@inheritDoc}
     * Not supported yet!
     * @return 
     */
    @Override
    public NavigableSet<K> descendingKeySet()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * {@inheritDoc}
     * @param fromKey
     * @param fromInclusive
     * @param toKey
     * @param toInclusive
     * @return 
     */
    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
    {
        Entry<K, V> from = entry(fromKey, null);
        Entry<K, V> to = entry(toKey, null);
        return new BinaryMap<>(entrySet.subSet(from, fromInclusive, to, toInclusive), comparator);
    }
    /**
     * {@inheritDoc}
     * @param toKey
     * @param inclusive
     * @return 
     */
    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
    {
        Entry<K, V> to = entry(toKey, null);
        return new BinaryMap<>(entrySet.headSet(to, inclusive), comparator);
    }
    /**
     * {@inheritDoc}
     * @param fromKey
     * @param inclusive
     * @return 
     */
    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
    {
        Entry<K, V> from = entry(fromKey, null);
        return new BinaryMap<>(entrySet.tailSet(from, inclusive), comparator);
    }
    /**
     * {@inheritDoc}
     * @param fromKey
     * @param toKey
     * @return 
     */
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey)
    {
        return subMap(fromKey, true, toKey, false);
    }
    /**
     * {@inheritDoc}
     * @param toKey
     * @return 
     */
    @Override
    public SortedMap<K, V> headMap(K toKey)
    {
        return headMap(toKey, false);
    }
    /**
     * {@inheritDoc}
     * @param fromKey
     * @return 
     */
    @Override
    public SortedMap<K, V> tailMap(K fromKey)
    {
        return tailMap(fromKey, true);
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override   
    public Comparator<? super K> comparator()
    {
        return comparator;
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public K firstKey()
    {
        return entrySet.first().getKey();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public K lastKey()
    {
        return entrySet.last().getKey();
    }
    
    public class NaturalImpl<K extends Comparable<K>,V> extends BaseEntry<K,V>
    {
        private NaturalImpl(K key, V value)
        {
            super(key, value);
        }

        @Override
        public int compareTo(Entry<K, V> o)
        {
            return key.compareTo(o.getKey());
        }
        
    }
    public class EntryImpl<K,V> extends BaseEntry<K,V>
    {
        private Comparator<Entry<K,V>> comparator;

        private EntryImpl(Comparator<Entry<K, V>> comparator, K key, V value)
        {
            super(key, value);
            this.comparator = comparator;
        }
        
        @Override
        public int compareTo(Entry<K, V> o)
        {
            return comparator.compare(this, o);
        }
        
    }
    private abstract class BaseEntry<K,V> implements Entry<K,V>, Comparable<Entry<K,V>>
    {
        protected K key;
        protected V value;

        public BaseEntry(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public String toString()
        {
            return "Entry{" + "key=" + key + ", value=" + value + '}';
        }

    }
}
