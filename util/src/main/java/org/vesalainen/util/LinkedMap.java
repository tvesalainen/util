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

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

/**
 * A NavigableMap implementation which preserves key add order
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <V>
 */
public class LinkedMap<K,V> implements NavigableMap<K,V>, Serializable
{
    private static final long serialVersionUID = 1L;
    private final LinkedSet<K> keySet = new LinkedSet<>();
    private final LinkedSet<Entry<K,V>> entrySet = new LinkedSet<>();
    private final List<V> values = new ArrayList<>();
    private final Map<K,V> map = new HashMap<>();
    
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    @Override
    public Entry<K, V> lowerEntry(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public K lowerKey(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entry<K, V> floorEntry(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public K floorKey(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entry<K, V> ceilingEntry(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public K ceilingKey(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entry<K, V> higherEntry(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public K higherKey(K key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entry<K, V> firstEntry()
    {
        return entrySet.first();
    }

    @Override
    public Entry<K, V> lastEntry()
    {
        return entrySet.last();
    }

    @Override
    public Entry<K, V> pollFirstEntry()
    {
        return entrySet.pollFirst();
    }

    @Override
    public Entry<K, V> pollLastEntry()
    {
        return entrySet.pollLast();
    }

    @Override
    public NavigableMap<K, V> descendingMap()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NavigableSet<K> navigableKeySet()
    {
        return keySet;
    }

    @Override
    public NavigableSet<K> descendingKeySet()
    {
        return keySet.descendingSet();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<K, V> headMap(K toKey)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comparator<? super K> comparator()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public K firstKey()
    {
        return keySet.first();
    }

    @Override
    public K lastKey()
    {
        return keySet.last();
    }

    @Override
    public Set<K> keySet()
    {
        return keySet;
    }

    @Override
    public Collection<V> values()
    {
        return values;
    }

    @Override
    public int size()
    {
        return keySet.size();
    }

    @Override
    public boolean isEmpty()
    {
        return keySet.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return keySet.contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return values.contains(value);
    }

    @Override
    public V get(Object key)
    {
        return map.get(key);
    }

    @Override
    public V put(K key, V value)
    {
        V removed = null;
        if (map.containsKey(key))
        {
            removed = remove(key);
        }
        map.put(key, value);
        keySet.add(key);
        entrySet.add(new SimpleEntry<>(key, value));
        values.add(value);
        return removed;
    }

    @Override
    public V remove(Object key)
    {
        V value = null;
        keySet.remove(key);
        Iterator<Entry<K, V>> ei = entrySet.iterator();
        while (ei.hasNext())
        {
            Entry<K, V> e = ei.next();
            if (key.equals(e.getKey()))
            {
                value = e.getValue();
                ei.remove();
                break;
            }
        }
        values.remove(value);
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for (Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear()
    {
        keySet.clear();
        entrySet.clear();
        values.clear();
        map.clear();
    }
    
}