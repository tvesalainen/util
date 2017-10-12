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
package org.vesalainen.math;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 * SymmetricDifferenceMatcher resolves problem where:
 * <p>
 * There are number of targets T1, T2, ... Ti
 * <p>
 * There are number of set of items S1, S2, ... Si. All Si items map to Ti.
 * <p>
 * There are number of subsets of Si SSi.
 * <p>
 * Problem is to map subsets SSi to targets Ti.
 * <p>
 * Problem is solved by keeping symmetric difference set of Sx. Every time a 
 * subset SSx resolves to target Tx the symmetric difference set grows making
 * further matches more probable.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SymmetricDifferenceMatcher<I,T>
{
    private final MapSet<I,T> mapSet = new HashMapSet<>();
    private final MapSet<T,I> reverseMap = new HashMapSet<>();
    private final Set<T> unresolved = Collections.unmodifiableSet(reverseMap.keySet());
    /**
     * Maps item to target 
     * @param item
     * @param target 
     */
    public void map(I item, T target)
    {
        mapSet.add(item, target);
        reverseMap.add(target, item);
    }
    /**
     * Maps collections of items to target
     * @param items
     * @param target 
     */
    public void map(Collection<I> items, T target)
    {
        for (I item : items)
        {
            map(item, target);
        }
    }
    /**
     * Returns target if one of collection items match. Otherwise returns null.
     * Matched targets mappings are removed.
     * @param items
     * @return 
     */
    public T match(Collection<I> items)
    {
        for (I item : items)
        {
            T match = match(item);
            if (match != null)
            {
                return match;
            }
        }
        return null;
    }
    /**
     * Returns target if item match. Otherwise returns null. 
     * Matched targets mappings are removed.
     * @param item
     * @return 
     */
    public T match(I item)
    {
        Set<T> set = mapSet.get(item);
        if (set.size() == 1)
        {
            T match = set.iterator().next();
            Set<I> items = reverseMap.get(match);
            items.forEach((i)->mapSet.removeItem(i, match));
            reverseMap.remove(match);
            return match;
        }
        return null;
    }
    /**
     * Matches A objects to T targets. Each A is mapped to subset of items. Each
     * match is passed to consumer. Map is traversed as long as there are matches.
     * Each match will remove targets mappings.
     * @param <A>
     * @param samples
     * @param consumer 
     */
    public <A> void match(Map<A,? extends Collection<I>> samples, BiConsumer<T, A> consumer)
    {
        Map<A,Collection<I>> m = new HashMap<>(samples);
        boolean cont = true;
        while (cont)
        {
            cont = false;
            Iterator<Entry<A, Collection<I>>> iterator = m.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<A, Collection<I>> e = iterator.next();
                T match = match(e.getValue());
                if (match != null)
                {
                    consumer.accept(match, e.getKey());
                    cont = true;
                    iterator.remove();
                }
            }
        }
    }
    /**
     * Returns an unmodifiable set of unresolved targets.
     * @return 
     */
    public Set<T> getUnresolved()
    {
        return unresolved;
    }
    
}
