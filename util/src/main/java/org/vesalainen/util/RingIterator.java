/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;

/**
 * RingIterator iterates all NavigableMap entries ascending starting from key and
 * ending entry before key.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingIterator<K,V> implements Iterator<Entry<K,V>>
{
    private Iterator<Entry<K,V>> head;
    private Iterator<Entry<K,V>> tail;

    public RingIterator(K key, NavigableMap<K,V> map)
    {
        tail = map.tailMap(key, true).entrySet().iterator();
        head = map.headMap(key, false).entrySet().iterator();
    }

    @Override
    public boolean hasNext()
    {
        return tail.hasNext() || head.hasNext();
    }

    @Override
    public Entry<K, V> next()
    {
        if (tail.hasNext())
        {
            return tail.next();
        }
        else
        {
            return head.next();
        }
    }
    
}
