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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <V>
 */
public abstract class AbstractMapList<K,V> implements MapList<K,V>, Serializable
{
    private static final long serialVersionUID = 1L;
    protected Map<K,List<V>> map;
    protected Comparator<V> comparator;

    public AbstractMapList()
    {
        this(new HashMap<>(), null);
    }

    public AbstractMapList(Map<K, List<V>> map, Comparator<V> comparator)
    {
        this.map = map;
        this.comparator = comparator;
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
    public List<V> put(K key, List<V> value)
    {
        return map.put(key, value);
    }

    @Override
    public List<V> remove(Object key)
    {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m)
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
    public Collection<List<V>> values()
    {
        return map.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet()
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
    public List<V> getOrDefault(Object key, List<V> defaultValue)
    {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super List<V>> action)
    {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super List<V>, ? extends List<V>> function)
    {
        map.replaceAll(function);
    }

    @Override
    public List<V> putIfAbsent(K key, List<V> value)
    {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, List<V> oldValue, List<V> newValue)
    {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public List<V> replace(K key, List<V> value)
    {
        return map.replace(key, value);
    }

    @Override
    public List<V> computeIfAbsent(K key, Function<? super K, ? extends List<V>> mappingFunction)
    {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public List<V> computeIfPresent(K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction)
    {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public List<V> compute(K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction)
    {
        return map.compute(key, remappingFunction);
    }

    @Override
    public List<V> merge(K key, List<V> value, BiFunction<? super List<V>, ? super List<V>, ? extends List<V>> remappingFunction)
    {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public void add(K key, V value)
    {
        add(key, -1, value);
    }
    
    @Override
    public void add(K key, int index, V value)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = createList();
            put(key, list);
        }
        if (index != -1)
        {
            list.add(index, value);
        }
        else
        {
            list.add(value);
        }
    }
    @Override
    public List<V> set(K key, Collection<V> value)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = createList();
            put(key, list);
        }
        list.clear();
        list.addAll(value);
        return list;
    }

    @Override
    public List<V> get(Object key)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return list;
        }
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
        List<V> list = get(key);
        boolean res = list.remove(value);
        if (list.isEmpty())
        {
            remove(key);
        }
        return res;
    }

    @Override
    public void addAll(K key, Collection<V> collection)
    {
        for (V value : collection)
        {
            add(key, value);
        }
    }

    @Override
    public boolean contains(K key, V value)
    {
        List<V> list = get(key);
        if (list == null)
        {
            return false;
        }
        return list.contains(value);
    }
    
    protected List<V> createList()
    {
        if (comparator != null)
        {
            return new OrderedList<>(comparator);
        }
        else
        {
            return new ArrayList<>();
        }
    }

    @Override
    public Comparator<V> getComparator()
    {
        return comparator;
    }

    @Override
    public String toString()
    {
        return map.toString();
    }
    
}
