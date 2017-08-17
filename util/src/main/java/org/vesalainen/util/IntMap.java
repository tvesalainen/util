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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * IntMap is a map-like class that can be used to store mappings to primitive
 * type.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>
 */
public class IntMap<K> extends AbstractPrimitiveMap<K,IntReference> implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * Creates a LongMap backed by HashMap
     */
    public IntMap()
    {
        super(new HashMap<>());
    }
    /**
     * Create a IntMap backed by given map
     * @param map 
     */
    public IntMap(Map<K, IntReference> map)
    {
        super(map);
    }
    
    /**
     * Associates value to key
     * @param key
     * @param value 
     */
    public void put(K key, int value)
    {
        IntReference w = map.get(key);
        if (w == null)
        {
            w = Recycler.get(IntReference.class, (IntReference r)->r.setValue(value));
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
    public void putAll(IntMap<K> m)
    {
        m.entrySet().stream().forEach((e) ->
        {
            put(e.getKey(), e.getValue().value);
        });
    }
    /**
     * Returns int value associated with key or throws IllegalArgumentException
     * @param key
     * @return 
     */
    public int getInt(K key)
    {
        IntReference w = map.get(key);
        if (w != null)
        {
            return w.value;
        }
        throw new IllegalArgumentException(key+" not found");
    }
    @Override
    public Collection<IntReference> values()
    {
        return map.values();
    }
}
