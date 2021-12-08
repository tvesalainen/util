/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K>   Key1
 * @param <L>   Key2
 * @param <V>   Value
 */
public interface Map2D<K,L,V>
{
    void clear();
    boolean containsKey(K key1, L key2);
    boolean containsValue(V value);
    boolean equals(Object o);
    V get(K key1, L key2);
    V getOrCreate(K key1, L key2);
    int	hashCode();
    boolean isEmpty();
    V put(K key1, L key2, V value);
    V remove(K key1, L key2);
    int size();
    public void forEach(Consumer2D<K,L,V> act);
    
    @FunctionalInterface
    public interface Consumer2D<K,L,V>
    {
        void accept(K key1, L key2, V value);
    }
    
}
