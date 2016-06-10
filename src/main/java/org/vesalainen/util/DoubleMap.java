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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DoubleMap is a map-like class that can be used to store mappings to primitive
 * type.
 * @author tkv
 * @param <K>
 */
public class DoubleMap<K> extends AbstractPrimitiveMap<K,DoubleReference> implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * Creates DoubleMap backed by HashMap
     */
    public DoubleMap()
    {
        super(new HashMap<>());
    }
    /**
     * Creates a DoubleMap backed by given map.
     * @param map 
     */
    public DoubleMap(Map<K, DoubleReference> map)
    {
        super(map);
    }
    
    /**
     * Associates value to key
     * @param key
     * @param value 
     */
    public void put(K key, double value)
    {
        DoubleReference w = map.get(key);
        if (w == null)
        {
            w = Recycler.get(DoubleReference.class, (DoubleReference r)->r.setValue(value));
            map.put(key, w);
        }
        else
        {
            w.value = value;
        }
    }
    /**
     * Associates values of another primitive map
     * @param m 
     */
    public void putAll(DoubleMap<K> m)
    {
        m.entrySet().stream().forEach((e) ->
        {
            put(e.getKey(), e.getValue().value);
        });
    }
    /**
     * Returns double value associated to key or NaN if none exists
     * @param key
     * @return 
     */
    public double getDouble(K key)
    {
        DoubleReference w = map.get(key);
        if (w != null)
        {
            return w.value;
        }
        return Double.NaN;
    }

    @Override
    public Collection<DoubleReference> values()
    {
        return map.values();
    }
}
