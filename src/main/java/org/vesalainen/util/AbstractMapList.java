/*
 * Copyright (C) 2015 tkv
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tkv
 * @param <M>
 * @param <L>
 */
public class AbstractMapList<M,L> implements MapList<M, L>
{
    private final Map<M,List<L>> map;
    
    private Comparator<L> comparator;
    private final List<L> emptyList = new ArrayList<>();

    public AbstractMapList(Map<M, List<L>> map)
    {
        this.map = map;
    }

    public AbstractMapList(Map<M, List<L>> map, Comparator<L> comparator)
    {
        this.map = map;
        this.comparator = comparator;
    }
    
    private List<L> createList()
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
    public void add(M key, L value)
    {
        add(key, -1, value);
    }
    
    @Override
    public void add(M key, int index, L value)
    {
        List<L> list = map.get(key);
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
    public List<L> set(M key, Collection<L> value)
    {
        List<L> list = map.get(key);
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
    public List<L> get(Object key)
    {
        List<L> list = map.get(key);
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
    public void addAll(Map<M, L> map)
    {
        for (Map.Entry<M, L> entry : map.entrySet())
        {
            add(entry.getKey(), entry.getValue());
        }
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
    public List<L> put(M key, List<L> value)
    {
        return map.put(key, value);
    }

    @Override
    public List<L> remove(Object key)
    {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends M, ? extends List<L>> m)
    {
        map.putAll(m);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public Set<M> keySet()
    {
        return map.keySet();
    }

    @Override
    public Collection<List<L>> values()
    {
        return map.values();
    }

    @Override
    public Set<Entry<M, List<L>>> entrySet()
    {
        return map.entrySet();
    }

    
}
