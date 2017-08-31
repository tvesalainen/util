/*
 * Copyright (C) 2012 Timo Vesalainen
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
import java.util.Set;

/**
 * MapList is a convenience interface for classes handling mapped sets. Set creation is automatic.
 * @author Timo Vesalainen
 * @param <K> Map key type
 * @param <V> Set value type
 */
public interface MapSet<K, V> extends MapCollection<K,Set<V>,V>
{
    /**
     * Replaces mapped Set with collection.
     * New set is returned.
     * @param key
     * @param value
     * @return 
     */
    @Override
    Set<V> set(K key, Collection<V> value);
}
