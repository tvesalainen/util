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

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author tkv
 * @param <K>
 * @param <V>
 */
public class TreeMapList<K,V> extends AbstractMapList<K,V>
{
    /**
     * Creates a TreeMapList with natural ordering.
     */
    public TreeMapList()
    {
        this(null, null);
    }
    /**
     * Creates a TreeMapList
     * @param mapComparator Comparator for keys. If null uses natural order
     * @param listComparator Comparator for List entries. If null uses no ordering
     */
    public TreeMapList(Comparator<K> mapComparator, Comparator<V> listComparator)
    {
        super(new TreeMap<K,List<V>>(mapComparator), listComparator);
    }

}
