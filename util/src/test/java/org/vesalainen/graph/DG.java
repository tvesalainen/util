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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class DG extends DiGraph<V>
{
    
    int entries;
    int edges;

    @Override
    protected void exit(V x, int depth)
    {
        System.err.println("exit(" + x + ", " + depth + ")");
    }

    @Override
    protected void pop(V s)
    {
        System.err.println("pop(" + s + ")");
    }

    @Override
    protected void branch(V x)
    {
        System.err.println("branch(" + x + ")");
    }

    @Override
    protected void edge(V from, V to)
    {
        System.err.println("edge(" + from + " -> " + to + ")");
        edges++;
    }

    @Override
    protected void enter(V x)
    {
        System.err.println("enter(" + x + ")");
        entries++;
    }
    
}
