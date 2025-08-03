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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
public class MapCollections
{
    /**
     * Returns unmodifiable MapList.
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static final <K,V> MapList<K,V> unmodifiableMapList(MapList<K,V> map)
    {
        return new UnmodifiableMapList(Collections.unmodifiableMap(map), map.getComparator());
    }
    public static class UnmodifiableMapList<K,V> extends AbstractMapList<K,V>
    {
        public UnmodifiableMapList(Map<K, List<V>> map, Comparator<V> comparator)
        {
            super(map, comparator);
        }

        @Override
        public void addAll(K key, Collection<V> collection)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeItem(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAll(Map<K, V> map)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> get(Object key)
        {
            List<V> list = super.get(key);
            return list != null ? Collections.unmodifiableList(list) : null;
        }

        @Override
        public List<V> set(K key, Collection<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(K key, int index, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> merge(K key, List<V> value, BiFunction<? super List<V>, ? super List<V>, ? extends List<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> compute(K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> computeIfPresent(K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> computeIfAbsent(K key, Function<? super K, ? extends List<V>> mappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> replace(K key, List<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, List<V> oldValue, List<V> newValue)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> putIfAbsent(K key, List<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super List<V>, ? extends List<V>> function)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> getOrDefault(Object key, List<V> defaultValue)
        {
            List<V> list = super.getOrDefault(key, defaultValue);
            return list != null ? Collections.unmodifiableList(list) : null;
        }

        @Override
        public int hashCode()
        {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            return super.equals(o);
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet()
        {
            return Collections.unmodifiableSet(super.entrySet());
        }

        @Override
        public Collection<List<V>> values()
        {
            return Collections.unmodifiableCollection(super.values());
        }

        @Override
        public Set<K> keySet()
        {
            return Collections.unmodifiableSet(super.keySet());
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> m)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> put(K key, List<V> value)
        {
            throw new UnsupportedOperationException();
        }
        
    }
    /**
     * Returns unmodifiable MapSet.
     * @param <K>
     * @param <V>
     * @param set
     * @return 
     */
    public static final <K,V> MapSet<K,V> unmodifiableMapSet(MapSet<K,V> set)
    {
        return new UnmodifiableMapSet(set);
    }
    public static class UnmodifiableMapSet<K,V> extends AbstractMapSet<K,V>
    {
        private Map<K, Set<V>> set;
        
        public UnmodifiableMapSet(Map<K, Set<V>> set)
        {
            super(set);
            this.set = set;
        }

        @Override
        protected Set<V> createSet()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> merge(K key, Set<V> value, BiFunction<? super Set<V>, ? super Set<V>, ? extends Set<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> compute(K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> computeIfPresent(K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> computeIfAbsent(K key, Function<? super K, ? extends Set<V>> mappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> replace(K key, Set<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, Set<V> oldValue, Set<V> newValue)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> putIfAbsent(K key, Set<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super Set<V>, ? extends Set<V>> function)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> getOrDefault(Object key, Set<V> defaultValue)
        {
            Set<V> set = super.getOrDefault(key, defaultValue);
            return  set != null ? Collections.unmodifiableSet(set) : null;
        }

        @Override
        public Set<Entry<K, Set<V>>> entrySet()
        {
            return Collections.unmodifiableSet(super.entrySet());
        }

        @Override
        public Collection<Set<V>> values()
        {
            return Collections.unmodifiableCollection(super.values());
        }

        @Override
        public Set<K> keySet()
        {
            return Collections.unmodifiableSet(super.keySet());
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends Set<V>> m)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> put(K key, Set<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> get(Object key)
        {
            Set<V> set = super.get(key);
            return set != null ? Collections.unmodifiableSet(set) : null;
        }

        @Override
        public boolean removeItem(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAll(Map<K, V> map)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<V> set(K key, Collection<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAll(K key, Collection<V> value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

    }
}
