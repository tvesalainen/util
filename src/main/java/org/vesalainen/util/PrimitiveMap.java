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

import java.util.Map;
import java.util.Set;

/**
 *
 * @author tkv
 * @param <K>
 * @param <V>
 */
public class PrimitiveMap<K,V>
{
    protected final Map<K, V> map;

    protected PrimitiveMap(Map<K, V> map)
    {
        this.map = map;
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        return map.entrySet();
    }
    
    public void clear()
    {
        map.clear();
    }

    public boolean containsKey(K key)
    {
        return map.containsKey(key);
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public Set<K> keySet()
    {
        return map.keySet();
    }

    public void remove(K key)
    {
        map.remove(key);
    }

    public int size()
    {
        return map.size();
    }
    
}
