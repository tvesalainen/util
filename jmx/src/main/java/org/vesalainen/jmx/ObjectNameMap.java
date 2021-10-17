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
package org.vesalainen.jmx;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class ObjectNameMap<T> implements Map<ObjectName,T>
{
    private Map<String,Map<ObjectName,T>> domains = Collections.synchronizedSortedMap(new TreeMap<>());
    
    public String[] getDomains()
    {
        List<String> list = domains.keySet().stream().collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
    {
        if (name.isDomainPattern())
        {
            return domains
                    .keySet()
                    .stream()
                    .map((d)->queryNames(d, name, query))
                    .flatMap((s)->s.stream())
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        else
        {
            return queryNames(name.getDomain(), name, query);
        }
    }

    private Set<ObjectName> queryNames(String domain, ObjectName name, QueryExp query)
    {
        return queryNames(domains.get(domain), name, query);
    }

    private Set<ObjectName> queryNames(Map<ObjectName,T> map, ObjectName name, QueryExp query)
    {
        if (map != null)
        {
            return map
                .keySet()
                .stream()
                .filter((n)->match(n, name, query))
                .collect(Collectors.toCollection(TreeSet::new));
        }
        else
        {
            return Collections.EMPTY_SET;
        }
    }

    private boolean match(ObjectName target, ObjectName name, QueryExp query)
    {
        try
        {
            return (name == null || name.apply(target)) && (query == null || query.apply(target));
        }
        catch (BadStringOperationException | BadBinaryOpValueExpException | BadAttributeValueExpException | InvalidApplicationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Override
    public int size()
    {
        return domains.values().stream().mapToInt((m)->m.size()).sum();
    }

    @Override
    public boolean isEmpty()
    {
        return domains.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key instanceof ObjectName)
        {
            ObjectName on = (ObjectName) key;
            if (on.isPattern())
            {
                throw new IllegalArgumentException(key+" pattern not allowed here");
            }
            else
            {
                Map<ObjectName, T> m = domains.get(on.getDomain());
                if (m != null)
                {
                    return m.containsKey(on);
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        return domains.values().stream().anyMatch((m)->m.containsValue(value));
    }

    @Override
    public T get(Object key)
    {
        if (key instanceof ObjectName)
        {
            ObjectName on = (ObjectName) key;
            if (on.isPattern())
            {
                throw new IllegalArgumentException(key+" pattern not allowed here");
            }
            else
            {
                Map<ObjectName, T> m = domains.get(on.getDomain());
                if (m != null)
                {
                    return m.get(on);
                }
            }
        }
        return null;
    }

    @Override
    public T put(ObjectName key, T value)
    {
        if (key instanceof ObjectName)
        {
            ObjectName on = (ObjectName) key;
            if (on.isPattern())
            {
                throw new IllegalArgumentException(key+" pattern not allowed here");
            }
            else
            {
                Map<ObjectName, T> m = domains.get(on.getDomain());
                if (m == null)
                {
                    m = Collections.synchronizedSortedMap(new TreeMap<>());
                    domains.put(on.getDomain(), m);
                }
                return m.put(on, value);
            }
        }
        return null;
    }

    @Override
    public T remove(Object key)
    {
        if (key instanceof ObjectName)
        {
            ObjectName on = (ObjectName) key;
            if (on.isPattern())
            {
                throw new IllegalArgumentException(key+" pattern not allowed here");
            }
            else
            {
                Map<ObjectName, T> m = domains.get(on.getDomain());
                if (m == null)
                {
                    m = Collections.synchronizedSortedMap(new TreeMap<>());
                    domains.put(on.getDomain(), m);
                }
                T removed = m.remove(on);
                if (m.isEmpty())
                {
                    domains.remove(on.getDomain());
                }
                return removed;
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends ObjectName, ? extends T> m)
    {
        m.forEach((o, t)->put(o, t));
    }

    @Override
    public void clear()
    {
        domains.clear();
    }

    @Override
    public Set<ObjectName> keySet()
    {
        return domains
                .values()
                .stream()
                .map((m)->m.keySet())
                .flatMap((c)->c.stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Collection<T> values()
    {
        return domains
                .values()
                .stream()
                .map((m)->m.values())
                .flatMap((c)->c.stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Entry<ObjectName, T>> entrySet()
    {
        return domains
                .values()
                .stream()
                .map((m)->m.entrySet())
                .flatMap((c)->c.stream())
                .collect(Collectors.toSet());
    }
    
}
