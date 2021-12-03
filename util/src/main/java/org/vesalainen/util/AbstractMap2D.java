/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 * @param <L>
 * @param <V>
 */
public class AbstractMap2D<K,L,V> implements Map2D<K,L,V>
{
    protected Map<K,Map<L,V>> map;
    protected Supplier<Map<L,V>> mapCreator;
    protected Supplier<V> itemCreator;

    protected AbstractMap2D(Supplier<Map<L, V>> mapCreator)
    {
        this(mapCreator, null);
    }
    protected AbstractMap2D(Supplier<Map<L, V>> mapCreator, Supplier<V> itemCreator)
    {
        this.map = (Map<K, Map<L, V>>) mapCreator.get();
        this.mapCreator = mapCreator;
        this.itemCreator = itemCreator;
    }
    
    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public boolean containsKey(K key1, L key2)
    {
        Map<L, V> m = map.get(key1);
        if (m != null)
        {
            return m.containsKey(key2);
        }
        return false;
    }

    @Override
    public boolean containsValue(V value)
    {
        for (Entry<K, Map<L, V>> entry : map.entrySet())
        {
            if (entry.getValue().containsValue(value))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key1, L key2)
    {
        Map<L, V> m = map.get(key1);
        if (m != null)
        {
            return m.get(key2);
        }
        return null;
    }
    /**
     * Returns item or creates it.
     * @param key1
     * @param key2
     * @return 
     */
    @Override
    public V getOrCreate(K key1, L key2)
    {
        Map<L, V> m = map.get(key1);
        if (m != null)
        {
            return m.get(key2);
        }
        else
        {
            if (itemCreator != null)
            {
                V item = itemCreator.get();
                put(key1, key2, item);
                return item;
            }
            else
            {
                throw new UnsupportedOperationException("itemCreator not supported on init");
            }
        }
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public V put(K key1, L key2, V value)
    {
        Map<L, V> m = map.get(key1);
        if (m == null)
        {
            m = mapCreator.get();
            map.put(key1, m);
        }
        return m.put(key2, value);
    }

    @Override
    public V remove(K key1, L key2)
    {
        Map<L, V> m = map.get(key1);
        if (m != null)
        {
            V removed = m.remove(key2);
            if (m.isEmpty())
            {
                map.remove(key1);
            }
            return removed;
        }
        return null;
    }

    @Override
    public int size()
    {
        int size = 0;
        for (Entry<K, Map<L, V>> entry : map.entrySet())
        {
            size += entry.getValue().size();
        }
        return size;
    }

    public void forEach(Consumer2D<K,L,V> act)
    {
        map.forEach((k,m)->
            {
                m.forEach((l,v)->
                {
                    act.accept(k, l, v);
                });
            });
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        forEach((k,l,v)->
        {
            sb.append('\n');
            sb.append(k);
            sb.append(", ");
            sb.append(l);
            sb.append(", ");
            sb.append(v);
        });
        return sb.toString();
    }
    
}
