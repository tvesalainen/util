/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * ArrayGridFiller acts roughly like fill tool in paint application
 * <p>Strategy function tells how fill is done. There are predefined strategies.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T> Color type. Don't have to be getColor.
 */
public class ArrayGridFiller<T>
{
    protected SimpleArrayGrid<T> grid;
    protected int stackSize;
    protected Strategy strategy;
    protected BitSet visited;
    protected int[] stack;
    protected int stackPtr;
    protected List<FillConsumer> consumers = new ArrayList<>();
    
    public ArrayGridFiller(SimpleArrayGrid<T> grid, Strategy strategy)
    {
        this.grid = grid;
        this.stackSize = 2*(grid.width()+grid.height());
        this.strategy = strategy;
    }
    public static final <T> void allDirections(int x, int y, T color, ArrayGridFiller<T> filler)
    {
        filler.push(x, y+1);     // south
        filler.push(x-1, y+1);   // south east
        filler.push(x-1, y);     // east
        filler.push(x-1, y-1);   // north east
        filler.push(x, y-1);     // north
        filler.push(x+1, y-1);   // north west
        filler.push(x+1, y);     // west
        filler.push(x+1, y+1);   // south west
    }
    public static final <T> void roundedSquare(int x, int y, T color, ArrayGridFiller<T> filler)
    {
        boolean se = filler.hit(x+1, y+1, color);   // south east
        boolean ne = filler.hit(x+1, y-1, color);   // north east
        boolean nw = filler.hit(x-1, y-1, color);   // north west
        boolean sw = filler.hit(x-1, y+1, color);   // south west
        if (se && sw)
        {
            filler.push(x, y+1);     // south
        }
        if (se && ne)
        {
            filler.push(x+1, y);     // east
        }
        if (ne && nw)
        {
            filler.push(x, y-1);     // north
        }
        if (sw && nw)
        {
            filler.push(x-1, y);     // west
        }
    }
    public static final <T> void square(int x, int y, T color, ArrayGridFiller<T> filler)
    {
        boolean se = filler.hit(x-1, y+1, color);   // south east
        boolean ne = filler.hit(x-1, y-1, color);   // north east
        boolean nw = filler.hit(x+1, y-1, color);   // north west
        boolean sw = filler.hit(x+1, y+1, color);   // south west
        if (se || sw)
        {
            filler.push(x, y+1);     // south
        }
        if (se || ne)
        {
            filler.push(x-1, y);     // east
        }
        if (ne || nw)
        {
            filler.push(x, y-1);     // north
        }
        if (sw || nw)
        {
            filler.push(x+1, y);     // west
        }
    }
    public void addConsumer(FillConsumer consumer)
    {
        consumers.add(consumer);
    }
    public void removeConsumer(FillConsumer consumer)
    {
        consumers.remove(consumer);
    }
    public BitGrid fill(int initX, int initY, T color)
    {
        visited = new BitSet();
        BitGrid result = new BitGrid(grid.width, grid.height, grid.boxed);

        stack = new int[stackSize];
        push(initX, initY);
        while (true)
        {
            int position = pop();
            if (position == -1)
            {
                return result;
            }
            visited.set(position);
            if (grid.hit(position, color))
            {
                int x = grid.column(position);
                int y = grid.line(position);
                for (FillConsumer consumer : consumers)
                {
                    consumer.fill(x, y, color);
                }
                strategy.apply(x, y, color, this);
                result.setColor(position, true);
            }
        }
    }

    public boolean hit(int x, int y, T color)
    {
        return grid.hit(x, y, color);
    }
    
    private void push(int x, int y)
    {
        if (!grid.inBox(x, y))
        {
            return;
        }
        int position = grid.position(x, y);
        if (position < 0 || position >= grid.length || visited.get(position))
        {
            return;
        }
        for (int ii=0;ii<stackPtr;ii++)
        {
            if (stack[ii] == position)
            {
                return;
            }
        }
        if (stackPtr >= stack.length)
        {
            stack = Arrays.copyOf(stack, 2*stack.length);
        }
        stack[stackPtr++] = position;
    }
    private int pop()
    {
        if (stackPtr > 0)
        {
            return stack[--stackPtr];
        }
        return -1;
    }
    @FunctionalInterface
    public interface FillConsumer<T>
    {
        void fill(int x, int y, T color);
    }
    @FunctionalInterface
    public interface Strategy<T>
    {
        void apply(int x, int y, T color, ArrayGridFiller<T> filler);
    }
}
