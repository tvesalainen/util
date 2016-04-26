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
import java.util.HashMap;
import java.util.Map;

/**
 * A Comparator that orders items in same order as given array. Items which
 * don't have equal in order array are put first in natural order.
 * @author tkv
 * @param <T>
 */
public class ArrayBasedComparator<T> implements Comparator<T>
{
    private Map<T,Integer> map;

    public ArrayBasedComparator(T... order)
    {
        map = new HashMap<>();
        int len = order.length;
        for (int ii=0;ii<len;ii++)
        {
            map.put(order[ii], ii);
        }
    }
    

    @Override
    public int compare(T o1, T o2)
    {
        int i1 = map.getOrDefault(o1, -1);
        int i2 = map.getOrDefault(o2, -1);
        if (i1 == -1 && i2 == -1)
        {
            Comparable<T> c = (Comparable<T>) o1;
            return c.compareTo(o2);
        }
        return i1 - i2;
    }
    
}
