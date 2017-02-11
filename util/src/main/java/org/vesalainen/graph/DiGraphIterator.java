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
package org.vesalainen.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * DiGraphIterator implements iterator over all vertices a given vertex has a connection.
 * This is an implementation of DiGraph algorithm.
 * @author Timo Vesalainen
 * @see DiGraph
 */
public final class DiGraphIterator<X> implements Iterator<X>
{
    private static final int INFINITY = 9999999;

    private Deque<X> stack = new ArrayDeque<>();
    private Map<X,Integer> indexMap = new HashMap<>();
    private Deque<Ctx> context = new ArrayDeque<>();
    private X next;
    private Function<? super X, ? extends Collection<X>> edges;
    /**
     * Creates a DiGraphIterator. It will iterate once for every node.
     * @param root
     * @param edges 
     */
    public DiGraphIterator(X root, Function<? super X, ? extends Collection<X>> edges)
    {
        this.edges = edges;
        next = enter(root);
    }
    /**
     * Creates a stream.
     * @param <T>
     * @param root
     * @param edges
     * @return 
     */
    public static <T> Stream<T> stream(T root, Function<? super T, ? extends Collection<T>> edges)
    {
        return StreamSupport.stream(spliterator(root, edges), false);
    }
    /**
     * Creates a Spliterator
     * @param <T>
     * @param root
     * @param edges
     * @return 
     */
    public static <T> Spliterator<T> spliterator(T root, Function<? super T, ? extends Collection<T>> edges)
    {
        DiGraphIterator dgi = new DiGraphIterator(root, edges);
        return Spliterators.spliteratorUnknownSize(dgi, 0);
    }
    
    @Override
    public boolean hasNext()
    {
        return next != null;
    }

    @Override
    public X next()
    {
        if (next == null)
        {
            throw new NoSuchElementException();
        }
        X n = next;
        next = traverse();
        return n;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    private X enter(X x)
    {
        stack.push(x);

        int d = stack.size();
        setIndexOf(x, d);

        Iterator<X> i = edges.apply(x).iterator();
        context.push(new Ctx(x, i, d));
        return x;
    }
    private X traverse()
    {
        while (!context.isEmpty())
        {
            Ctx ctx = context.peek();
            X x = ctx.x;
            while (ctx.i.hasNext())
            {
                X y = ctx.i.next();
                if (indexOf(y) == 0)
                {
                    return enter(y);
                }
                setIndexOf(x, Math.min(indexOf(x), indexOf(y)));
            }
            pop();
        }
        return null;
    }
    
    private void pop()
    {
        Ctx ctx = context.peek();
        X x = ctx.x;
        if (indexOf(x) == ctx.d)
        {
            X s = stack.peek();
            while (!s.equals(x))
            {
                stack.pop();
                setIndexOf(s, INFINITY);
                s = stack.peek();
            }
            setIndexOf(x, INFINITY);
            stack.pop();
        }
        ctx = context.pop();
    }
    
    private void setIndexOf(X state, int index)
    {
        indexMap.put(state, index);
    }
    
    private int indexOf(X state)
    {
        Integer i = indexMap.get(state);
        if (i == null)
        {
            return 0;
        }
        else
        {
            return i;
        }
    }
    private class Ctx
    {
        X x;
        Iterator<X> i;
        int d;

        public Ctx(X x, Iterator<X> i, int d)
        {
            this.x = x;
            this.i = i;
            this.d = d;
        }

    }
}
