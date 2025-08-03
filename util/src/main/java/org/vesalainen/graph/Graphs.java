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

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Algorithm implementations for graph traversal
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Graphs
{
    /**
     * Returns stream for graph nodes starting with root. Function edges returns
     * stream for each node containing nodes edges. Traversal is in breadth first
     * order.
     * @param <T>
     * @param root
     * @param edges
     * @return 
     */
    public static final <T> Stream<T> breadthFirst(T root, Function<? super T, ? extends Stream<T>> edges)
    {
        return BreadthFirst.stream(root, edges);
    }
    /**
     * Returns stream for graph nodes starting with root. Function edges returns
     * stream for each node containing nodes edges.
     * @param <T>
     * @param root
     * @param edges
     * @return 
     */
    public static final <T> Stream<T> diGraph(T root, Function<? super T, ? extends Stream<T>> edges)
    {
        return DiGraphIterator.stream(root, edges);
    }
    
}
