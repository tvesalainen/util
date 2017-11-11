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
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <V>
 */
public class BinaryMap<K,V> extends AbstractMap<K,V>
{
    private BinarySet<Entry<K, V>> entrySet;
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
    
    /**
     * {@inheritDoc}
     * @param key
     * @param value
     * @return 
     */
    @Override
    public V put(K key, V value)
    {
        Entry<K,V> oldEntry = entrySet.addEntry(entry(key, value));
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
            return new EntryImpl(comparator, key, value);
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
        Entry<K, V> entry = entry((K) key, null);
        Entry<K, V> res = entrySet.get(entry);
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
        Entry<K, V> entry = entry((K) key, null);
        return entrySet.get(entry, (e)->predicate.test(e.getKey(), e.getValue()));
    }
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
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
