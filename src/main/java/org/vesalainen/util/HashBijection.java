/*
 * Copyright (C) 2015 tkv
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class HashBijection<T,S> implements Bijection<T,S>, Serializable
{
    private static final long serialVersionUID = 1L;
    private final Map<T,S> m1 = new HashMap<>();
    private final Map<S,T> m2 = new HashMap<>();

    @Override
    public void put(T t, S s)
    {
        m1.put(t, s);
        m2.put(s, t);
    }

    @Override
    public T getFirst(S s)
    {
        return m2.get(s);
    }

    @Override
    public S getSecond(T t)
    {
        return m1.get(t);
    }

    @Override
    public boolean containsFirst(S s)
    {
        return m2.containsKey(s);
    }

    @Override
    public boolean containsSecond(T t)
    {
        return m1.containsKey(t);
    }
    public Set<Map.Entry<T, S>> entrySet()
    {
        return m1.entrySet();
    }

    @Override
    public Set<T> firstSet()
    {
        return m1.keySet();
    }

    @Override
    public Set<S> secondSet()
    {
        return m2.keySet();
    }

    @Override
    public S removeFirst(S s)
    {
        return m1.remove(s);
    }

    @Override
    public T removeSecond(T t)
    {
        return m2.remove(t);
    }
}
