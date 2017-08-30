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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnmodifiebleMapCollection<K,C,V> implements MapCollection<K, Collection<V>, V>
{
    private MapCollection<K, Collection<V>, V> inner;

    public UnmodifiebleMapCollection(MapCollection<K, Collection<V>, V> inner)
    {
        this.inner = inner;
    }

    @Override
    public void add(K key, V value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public void addAll(K key, Collection<V> value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public void addAll(Map<K, V> map)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public boolean contains(K key, V value)
    {
        return inner.contains(key, value);
    }

    @Override
    public Collection<V> set(K key, Collection<V> value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public boolean removeItem(K key, V value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Stream<V> allValues()
    {
        return inner.allValues();
    }

    @Override
    public int size()
    {
        return inner.size();
    }

    @Override
    public boolean isEmpty()
    {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return inner.containsValue(value);
    }

    @Override
    public Collection<V> get(Object key)
    {
        return inner.get(key);
    }

    @Override
    public Collection<V> put(K key, Collection<V> value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> remove(Object key)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public void putAll(Map<? extends K, ? extends Collection<V>> m)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Set<K> keySet()
    {
        return inner.keySet();
    }

    @Override
    public Collection<Collection<V>> values()
    {
        return inner.values();
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet()
    {
        return inner.entrySet();
    }

    @Override
    public Collection<V> getOrDefault(Object key, Collection<V> defaultValue)
    {
        return inner.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super Collection<V>> action)
    {
        inner.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super Collection<V>, ? extends Collection<V>> function)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> putIfAbsent(K key, Collection<V> value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public boolean replace(K key, Collection<V> oldValue, Collection<V> newValue)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> replace(K key, Collection<V> value)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> computeIfAbsent(K key, Function<? super K, ? extends Collection<V>> mappingFunction)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> computeIfPresent(K key, BiFunction<? super K, ? super Collection<V>, ? extends Collection<V>> remappingFunction)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> compute(K key, BiFunction<? super K, ? super Collection<V>, ? extends Collection<V>> remappingFunction)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }

    @Override
    public Collection<V> merge(K key, Collection<V> value, BiFunction<? super Collection<V>, ? super Collection<V>, ? extends Collection<V>> remappingFunction)
    {
        throw new UnsupportedOperationException("unmodifieble");
    }
    
}
