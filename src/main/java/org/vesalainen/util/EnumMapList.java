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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * EnumMap and ArrayList based implementation of MapList
 * @author tkv
 * @param <K> Map key type
 * @param <V> List value type
 */
public class EnumMapList<K extends Enum<K>,V> extends EnumMap<K,List<V>> implements MapList<K, V>
{
    private Comparator<V> comparator;
    private final List<V> emptyList = new ArrayList<>();

    public EnumMapList(Class<K> keyType)
    {
        super(keyType);
    }

    public EnumMapList(Class<K> keyType, Comparator<V> comparator)
    {
        super(keyType);
        this.comparator = comparator;
    }

    private List<V> createList()
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
    public void add(K key, V value)
    {
        add(key, -1, value);
    }
    
    @Override
    public void add(K key, int index, V value)
    {
        List<V> list = super.get(key);
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
        List<V> list = super.get(key);
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
        List<V> list = super.get(key);
        if (list == null)
        {
            return emptyList;
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

}
