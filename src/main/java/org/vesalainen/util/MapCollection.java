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

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author tkv
 * @param <K> Key type
 * @param <C> Collection type
 * @param <V> Value type
 */
public interface MapCollection<K,C extends Collection<V>,V> extends Map<K,C>
{
    /**
     * Adds a value to collection
     * @param key
     * @param value 
     */
    void add(K key, V value);
    /**
     * Adds values to collection.
     * @param key
     * @param value 
     */
    void addAll(K key, Collection<V> value);
    /**
     * Adds all maps key value pairs.
     * @param map 
     */
    void addAll(Map<K,V> map);
    /**
     * Return true is mapped collection contains value.
     * @param key
     * @param value
     * @return 
     */
    boolean contains(K key, V value);
    /**
     * Replaces mapped collection with collection. New collection is in collection iterator order.
     * New collection is returned.
     * @param key
     * @param value
     * @return 
     */
    C set(K key, Collection<V> value);
    /**
     * Removes the item from collection. If collection becomes empty it is removed from 
     * map.
     * @param key
     * @param value
     * @return True if item was removed from list.
     */
    boolean removeItem(K key, V value);
}
