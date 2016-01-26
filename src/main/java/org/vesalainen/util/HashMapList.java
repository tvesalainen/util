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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HashMap and ArrayList based implementation of MapList
 * @author tkv
 * @param <M> Map key type
 * @param <L> List value type
 */
public class HashMapList<M,L> extends HashMap<M,List<L>> implements MapList<M, L>
{
    private Comparator<L> comparator;

    public HashMapList()
    {
    }

    public HashMapList(Comparator<L> comparator)
    {
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
        List<L> list = super.get(key);
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
        List<L> list = super.get(key);
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
        List<L> list = super.get(key);
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
    public void addAll(Map<M, L> map)
    {
        for (Entry<M, L> entry : map.entrySet())
        {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean removeItem(M key, L value)
    {
        List<L> list = get(key);
        boolean res = list.remove(value);
        if (list.isEmpty())
        {
            remove(key);
        }
        return res;
    }

}
