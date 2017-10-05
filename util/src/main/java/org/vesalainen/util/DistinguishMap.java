/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Set;
import org.vesalainen.util.immutable.UnmodifiableMap;

/**
 * DistinguishMap maintains several mappings from K to V but only unique mappings
 * are visible.
 * <p>
 * If A and B maps to foo and B and C maps to bar then only A to foo and C to bar
 * are visible. After you remove bar mappings B and C then A and B maps to foo.
 * <p>
 * This class is not thread safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DistinguishMap<K,V> extends UnmodifiableMap<K,V>
{
    private MapSet<K,V> mapList = new HashMapSet<>();
    /**
     * Map all keys values to value
     * @param keys
     * @param value 
     */
    public synchronized void add(Set<K> keys, V value)
    {
        keys.forEach((k)->mapList.add(k, value));
        calc();
    }
    /**
     * Unmap all keys values from value.
     * @param keys
     * @param value 
     */
    public synchronized void remove(Set<K> keys, V value)
    {
        keys.forEach((k)->mapList.removeItem(k, value));
        calc();
    }
    private void calc()
    {
        inner.clear();
        mapList.forEach((k,v)->
        {
            if (v.size() == 1)
            {
                inner.put(k, v.iterator().next());
            }
        });
    }
}
