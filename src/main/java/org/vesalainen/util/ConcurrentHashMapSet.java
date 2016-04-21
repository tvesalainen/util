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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * ConcurrentHashMap and ConcurrentSkipListSet based implementation of MapSet
 * @author tkv
 * @param <K> Map key type.
 * @param <V> Set value type
 */
public class ConcurrentHashMapSet<K,V> extends AbstractMapSet<K,V>
{

    public ConcurrentHashMapSet()
    {
        super(new ConcurrentHashMap<K,Set<V>>());
    }

    @Override
    protected Set<V> createSet()
    {
        return new ConcurrentSkipListSet<>();
    }
}