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

import java.util.Collection;
import java.util.Collections;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class V implements Vertex<V>
{
    
    String name;
    Collection<V> edges = Collections.EMPTY_LIST;

    public V(String name)
    {
        this.name = name;
    }

    @Override
    public Collection<V> edges()
    {
        return edges;
    }

    void set(V... es)
    {
        edges = CollectionHelp.create(es);
    }

    @Override
    public String toString()
    {
        return name;
    }
    
}
