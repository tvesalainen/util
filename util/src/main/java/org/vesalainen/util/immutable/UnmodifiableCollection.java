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
package org.vesalainen.util.immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnmodifiableCollection<T> implements Collection<T>
{
    private Collection<T> collection;

    public UnmodifiableCollection(Collection<T> collection)
    {
        this.collection = collection;
    }

    @Override
    public int size()
    {
        return collection.size();
    }

    @Override
    public boolean isEmpty()
    {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return collection.contains(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        return new UnmodifiableIterator(collection.iterator());
    }

    @Override
    public Object[] toArray()
    {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return collection.toArray(a);
    }

    @Override
    public boolean add(T e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o)
    {
        return collection.equals(o);
    }

    @Override
    public int hashCode()
    {
        return collection.hashCode();
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return collection.spliterator();
    }

    @Override
    public Stream<T> stream()
    {
        return collection.stream();
    }

    @Override
    public Stream<T> parallelStream()
    {
        return collection.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action)
    {
        collection.forEach(action);
    }
    
}
