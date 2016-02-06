/*
 * Copyright (C) 2012 Timo Vesalainen
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * HashMap and HashSet based implementation of MapSet
 * @author tkv
 * @param <K> Map key type.
 * @param <V> Set value type
 */
public class HashMapSet<K,V> extends HashMap<K,Set<V>> implements MapSet<K, V>
{

    protected Set<V> createSet()
    {
        return new HashSet<>();
    }
    @Override
    public boolean contains(K key, V value)
    {
        Set<V> set = get(key);
        if (set == null)
        {
            return false;
        }
        return set.contains(value);
    }

    @Override
    public void add(K key, V value)
    {
        Set<V> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        set.add(value);
    }

    @Override
    public void addAll(K key, Collection<V> value)
    {
        Set<V> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        if (value != null)
        {
            set.addAll(value);
        }
    }

    @Override
    public Set<V> set(K key, Collection<V> value)
    {
        Set<V> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        set.clear();
        if (value != null)
        {
            set.addAll(value);
        }
        return set;
    }

    @Override
    public void addAll(Map<K, V> map)
    {
        for (Entry<K, V> entry : map.entrySet())
        {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean removeItem(K key, V value)
    {
        Set<V> set = get(key);
        boolean res = set.remove(value);
        if (set.isEmpty())
        {
            remove(key);
        }
        return res;
    }

}
