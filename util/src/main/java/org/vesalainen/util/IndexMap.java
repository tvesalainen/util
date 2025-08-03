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

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ObjIntConsumer;

/**
 * IndexMap maps array indexes to T items.It is effective if indexes are from
 a short range.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class IndexMap<T>
{
    private T[] array;
    private int offset;

    public IndexMap(int offset, T... array)
    {
        this.array = array;
        this.offset = offset;
    }
    public IndexMap(Map<Integer,T> map)
    {
        Builder<T> builder = new Builder<>();
        map.forEach((i, t)->builder.put(0, t));
        IndexMap<T> im = builder.build();
        this.array = im.array;
        this.offset = im.offset;
    }
    public T get(int index)
    {
        int idx = index - offset;
        if (idx < 0 || idx >= array.length)
        {
            return null;
        }
        else
        {
            return array[idx];
        }
    }
    public void forEach(ObjIntConsumer<T> act)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            T item = array[ii];
            if (item != null)
            {
                act.accept(item, len+offset);
            }
        }
    }
    public static class Builder<T>
    {
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;
        private Map<Integer,T> map = new HashMap<>();
        private Class<T> cls;
        
        public Builder<T> put(int index, T item)
        {
            min = min(index, min);
            max = max(index, max);
            map.put(index, item);
            cls = (Class<T>) item.getClass();
            return this;
        }
        
        public IndexMap<T> build()
        {
            if (!map.isEmpty())
            {
                T[] array = (T[]) Array.newInstance(cls, max-min+1);
                map.forEach((i, t)->array[i-min] = t);
                return new IndexMap<>(min, array);
            }
            else
            {
                return new IndexMap<>(0, (T[])ArrayHelp.EMPTY);
            }
        }
    }
}
