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

import java.io.Serializable;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * EnumMap and ArrayList based implementation of MapList
 * @author tkv
 * @param <K> Map key type
 * @param <V> List value type
 */
public class EnumMapList<K extends Enum<K>,V> extends AbstractMapList<K,V> implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * Creates a EnumMapList with no ordering for list.
     * @param keyType Enum key type
     */
    public EnumMapList(Class<K> keyType)
    {
        this(keyType, null);
    }
    /**
     * Creates a EnumMapList
     * @param keyType Enum key type
     * @param comparator Comparator for List. If null no ordering.
     */
    public EnumMapList(Class<K> keyType, Comparator<V> comparator)
    {
        super(new EnumMap<K,List<V>>(keyType), comparator);
    }

}
