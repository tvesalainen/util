/*
 * Copyright (C) 2017 tkv
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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class GraphsTest
{

    private final V a;
    
    public GraphsTest()
    {
        a = new V("A");
        V b = new V("B");
        V c = new V("C");
        V d = new V("D");
        V e = new V("E");
        V f = new V("F");
        V g = new V("G");
        V h = new V("H");
        a.set(b);
        b.set(c, d);
        c.set(f, g, a);
        d.set(e);
        e.set(f);
        g.set(h);
    }

    @Test
    public void testDiGraph()
    {
        Stream<V> s = Graphs.diGraph(a, (t)->t.edges().stream());
        assertEquals("ABCFGHDE", s.map((t)->t.toString()).collect(Collectors.joining()));
    }
    
    @Test
    public void testBreadthFirst()
    {
        Stream<V> s = Graphs.breadthFirst(a, (t)->t.edges().stream());
        assertEquals("ABCDFGEH", s.map((t)->t.toString()).collect(Collectors.joining()));
    }
    
}
