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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K> Key type
 * @param <V> Value type
 */
public class TreeMapList<K,V> extends AbstractMapList<K,V> implements NavigableMap<K,List<V>>, Serializable
{
    private static final long serialVersionUID = 1L;
    protected NavigableMap<K,List<V>> navigableMap;
    /**
     * Creates a TreeMapList with natural ordering.
     */
    public TreeMapList()
    {
        this(null, null);
    }
    /**
     * Creates a TreeMapList
     * @param mapComparator Comparator for keys. If null uses natural order
     * @param listComparator Comparator for List entries. If null uses no ordering
     */
    public TreeMapList(Comparator<K> mapComparator, Comparator<V> listComparator)
    {
        super(new TreeMap<K,List<V>>(mapComparator), listComparator);
        navigableMap = (TreeMap<K, List<V>>) map;
    }

    @Override
    public Entry<K, List<V>> lowerEntry(K key)
    {
        return navigableMap.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key)
    {
        return navigableMap.lowerKey(key);
    }

    @Override
    public Entry<K, List<V>> floorEntry(K key)
    {
        return navigableMap.floorEntry(key);
    }

    @Override
    public K floorKey(K key)
    {
        return navigableMap.floorKey(key);
    }

    @Override
    public Entry<K, List<V>> ceilingEntry(K key)
    {
        return navigableMap.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key)
    {
        return navigableMap.ceilingKey(key);
    }

    @Override
    public Entry<K, List<V>> higherEntry(K key)
    {
            return navigableMap.higherEntry(key);
    }

    @Override
    public K higherKey(K key)
    {
        return navigableMap.higherKey(key);
    }

    @Override
    public Entry<K, List<V>> firstEntry()
    {
        return navigableMap.firstEntry();
    }

    @Override
    public Entry<K, List<V>> lastEntry()
    {
        return navigableMap.lastEntry();
    }

    @Override
    public Entry<K, List<V>> pollFirstEntry()
    {
        return navigableMap.pollFirstEntry();
    }

    @Override
    public Entry<K, List<V>> pollLastEntry()
    {
        return navigableMap.pollLastEntry();
    }

    @Override
    public NavigableMap<K, List<V>> descendingMap()
    {
        return navigableMap.descendingMap();
    }

    @Override
    public NavigableSet<K> navigableKeySet()
    {
        return navigableMap.navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet()
    {
        return navigableMap.descendingKeySet();
    }

    @Override
    public NavigableMap<K, List<V>> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
    {
        return navigableMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public NavigableMap<K, List<V>> headMap(K toKey, boolean inclusive)
    {
        return navigableMap.headMap(toKey, inclusive);
    }

    @Override
    public NavigableMap<K, List<V>> tailMap(K fromKey, boolean inclusive)
    {
        return navigableMap.tailMap(fromKey, inclusive);
    }

    @Override
    public SortedMap<K, List<V>> subMap(K fromKey, K toKey)
    {
        return navigableMap.subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<K, List<V>> headMap(K toKey)
    {
        return navigableMap.headMap(toKey);
    }

    @Override
    public SortedMap<K, List<V>> tailMap(K fromKey)
    {
        return navigableMap.tailMap(fromKey);
    }

    @Override
    public Comparator<? super K> comparator()
    {
        return navigableMap.comparator();
    }

    @Override
    public K firstKey()
    {
        return navigableMap.firstKey();
    }

    @Override
    public K lastKey()
    {
        return navigableMap.lastKey();
    }

    @Override
    public Set<K> keySet()
    {
        return navigableMap.keySet();
    }

    @Override
    public Collection<List<V>> values()
    {
        return navigableMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet()
    {
        return navigableMap.entrySet();
    }

}
