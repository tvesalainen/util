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
import java.util.HashMap;
import org.vesalainen.util.LongMap.LongReference;

/**
 * LongMap is a map-like class that can be used to store mappings to primitive
 * type.
 * @author tkv
 * @param <K>
 */
public class LongMap<K> extends AbstractPrimitiveMap<K,LongReference> implements Serializable
{
    private static final long serialVersionUID = 1L;

    public LongMap()
    {
        super(new HashMap<>());
    }
    /**
     * Associates value to key
     * @param key
     * @param value 
     */
    public void put(K key, long value)
    {
        LongReference w = map.get(key);
        if (w == null)
        {
            w = new LongReference();
            map.put(key, w);
        }
        w.value = value;
    }
    /**
     * Returns double value associated to key or throws IllegalArgumentException
     * @param key
     * @return 
     */
    public long getLong(K key)
    {
        LongReference w = map.get(key);
        if (w != null)
        {
            return w.value;
        }
        throw new IllegalArgumentException(key+" not found");
    }
    /**
     * A Reference to primitive value.
     * <p>Although this class seems immutable, It's value can change as an effect 
     * of put operation.
     */
    public final class LongReference
    {
        private long value;

        private LongReference()
        {
        }

        public long getValue()
        {
            return value;
        }
        
    }
}
