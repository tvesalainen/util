/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * IdentityArraySet implements Set by using ArrayList. This is suitable for small sets.
 * Equality is tested with identity.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IdentityArraySet<T> extends AbstractSet<T>
{
    private List<T> list = new ArrayList<>();

    @Override
    public boolean add(T e)
    {
        if (!contains(e))
        {
            list.add(e);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void forEach(Consumer<? super T> action)
    {
        list.forEach(action);
    }

    @Override
    public void clear()
    {
        list.clear();
    }

    @Override
    public boolean remove(Object o)
    {
        return list.remove(o);
    }

    @Override
    public boolean contains(Object o)
    {
        int size = list.size();
        for (int ii=0;ii<size;ii++)
        {
            if (o == list.get(ii))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return list.isEmpty();
    }
    
    
    @Override
    public Iterator<T> iterator()
    {
        return list.iterator();
    }

    @Override
    public int size()
    {
        return list.size();
    }
    
}
