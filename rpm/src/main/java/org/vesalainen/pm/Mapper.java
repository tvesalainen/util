/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can support using package dependent values for certain difficult
 * to map fields like application-area. PackageBuilderFactory sets key to 
 * requested package-builder name
 * @author tkv
 * @param <V>
 */
public class Mapper<V>
{
    private static String key;
    private Map<String,V> map = new HashMap<>();
    /**
     * Sets static key
     * @param key 
     */
    static void setKey(String key)
    {
        Mapper.key = key;
    }
    /**
     * Add key dependent value
     * @param key
     * @param value 
     */
    public void add(String key, V value)
    {
        map.put(key, value);
    }
    /**
     * Returns key dependent value
     * @return 
     */
    public V get()
    {
        return map.get(key);
    }
}
