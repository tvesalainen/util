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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * HashMap and ArrayList based implementation of MapList
 * @author tkv
 * @param <M> Map key type
 * @param <L> List value type
 */
public class HashMapList<M,L> extends AbstractMapList<M,L>
{
    public HashMapList()
    {
        super(new HashMap<M,List<L>>());
    }

    public HashMapList(Comparator<L> comparator)
    {
        super(new HashMap<M,List<L>>(), comparator);
    }

    @Override
    protected List<L> createList()
    {
        if (comparator != null)
        {
            return new OrderedList<>(comparator);
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
