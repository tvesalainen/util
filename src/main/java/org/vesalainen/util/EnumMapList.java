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
import java.util.HashMap;
import java.util.List;

/**
 * EnumMap and ArrayList based implementation of MapList
 * @author tkv
 * @param <M> Map key type
 * @param <L> List value type
 */
public class EnumMapList<M extends Enum<M>,L> extends EnumMap<M,List<L>> implements MapList<M, L>
{
    private Comparator<L> comparator;
    private final List<L> emptyList = new ArrayList<>();

    public EnumMapList(Class<M> keyType)
    {
        super(keyType);
    }

    public EnumMapList(Class<M> keyType, Comparator<L> comparator)
    {
        super(keyType);
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
            return emptyList;
        }
        else
        {
            return list;
        }
    }

}
