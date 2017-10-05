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
package org.vesalainen.util.immutable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A Map implementation where modifying methods throws UnsupportedOperationException
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnmodifiableMap<K,V> implements Map<K,V>
{
    private Map<K,V> map;
    private Set<Entry<K, V>> entrySet;
    private Set<K> keySet;
    private Collection<V> values;

    public UnmodifiableMap()
    {
        this(new HashMap<>());
    }

    public UnmodifiableMap(Map<K, V> map)
    {
        this.map = map;
        this.keySet = new UnmodifiableSet(map.keySet());
        this.entrySet = new UnmodifiableSet(map.entrySet());
        this.values = new UnmodifiableCollection(map.values());
    }
    

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key)
    {
        return map.get(key);
    }

    @Override
    public V put(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet()
    {
        return keySet;
    }

    @Override
    public Collection<V> values()
    {
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    @Override
    public boolean equals(Object o)
    {
        return map.equals(o);
    }

    @Override
    public int hashCode()
    {
        return map.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }
    
}
