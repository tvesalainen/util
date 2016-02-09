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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author tkv
 * @param <K>
 * @param <V>
 */
public abstract class AbstractMapSet<K,V> implements MapSet<K, V>
{
    protected Map<K,Set<V>> map;

    protected AbstractMapSet(Map<K, Set<V>> map)
    {
        this.map = map;
    }

    protected abstract Set<V> createSet();
    
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

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    @Override
    public Set<V> get(Object key)
    {
        return map.get(key);
    }

    @Override
    public Set<V> put(K key, Set<V> value)
    {
        return map.put(key, value);
    }

    @Override
    public Set<V> remove(Object key)
    {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Set<V>> m)
    {
        map.putAll(m);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public Set<K> keySet()
    {
        return map.keySet();
    }

    @Override
    public Collection<Set<V>> values()
    {
        return map.values();
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet()
    {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o)
    {
        return map.equals(o);
    }

    @Override
    public int hashCode()
    {
        return map.hashCode();
    }

    @Override
    public Set<V> getOrDefault(Object key, Set<V> defaultValue)
    {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super Set<V>> action)
    {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super Set<V>, ? extends Set<V>> function)
    {
        map.replaceAll(function);
    }

    @Override
    public Set<V> putIfAbsent(K key, Set<V> value)
    {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, Set<V> oldValue, Set<V> newValue)
    {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public Set<V> replace(K key, Set<V> value)
    {
        return map.replace(key, value);
    }

    @Override
    public Set<V> computeIfAbsent(K key, Function<? super K, ? extends Set<V>> mappingFunction)
    {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Set<V> computeIfPresent(K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction)
    {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Set<V> compute(K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction)
    {
        return map.compute(key, remappingFunction);
    }

    @Override
    public Set<V> merge(K key, Set<V> value, BiFunction<? super Set<V>, ? super Set<V>, ? extends Set<V>> remappingFunction)
    {
        return map.merge(key, value, remappingFunction);
    }
    
}
