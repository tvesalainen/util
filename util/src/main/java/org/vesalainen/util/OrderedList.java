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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * An ArrayList implementation which keeps list items ordered.
 * @author tkv
 * @param <T>
 */
public class OrderedList<T> extends ArrayList<T> implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final Comparator<T> comp;

    public OrderedList(Comparator<T> comp)
    {
        this.comp = comp;
    }

    @Override
    public boolean add(T e)
    {
        int index;
        for (index=0;index<size();index++)
        {
            if (comp.compare(get(index), e) < 0)
            {
                break;
            }
        }
        super.add(index, e);
        return true;
    }

    @Override
    public void add(int index, T element)
    {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        for (T t : c)
        {
            add(t);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException("not supported");
    }

}
