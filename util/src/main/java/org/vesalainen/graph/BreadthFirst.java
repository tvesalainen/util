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
package org.vesalainen.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BreadthFirst
{

    public static final <T> Stream<T> stream(T root, Function<? super T, ? extends Stream<T>> edges)
    {
        return StreamSupport.stream(new BFSpliterator<>(root, edges), false);
    }
    
    static class BFSpliterator<T> extends AbstractSpliterator<T>
    {
        private Set<T> set = new HashSet<>();
        private Deque<T> queue = new ArrayDeque<>();
        
        private final Function<? super T, ? extends Stream<T>> edges;

        public BFSpliterator(T root, Function<? super T, ? extends Stream<T>> edges)
        {
            super(Long.MAX_VALUE, 0);
            this.edges = edges;
            if (root != null)
            {
                set.add(root);
                queue.addLast(root);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action)
        {
            if (!queue.isEmpty())
            {
                T current = queue.removeFirst();
                action.accept(current);
                edges.apply(current)
                        .filter((t)->!set.contains(t))
                        .forEach((t)->{set.add(t);queue.addLast(t);});
                return true;
            }
            else
            {
                return false;
            }
        }
        
    }
}
