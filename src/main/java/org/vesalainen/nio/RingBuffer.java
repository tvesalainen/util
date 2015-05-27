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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.InvalidMarkException;

/**
 *
 * @author tkv
 * @param <B> Buffer type
 * @param <R> Reader type
 * @param <W> Writer type
 */
public abstract class RingBuffer<B extends Buffer,R,W>
{
    protected final B buffer;
    private int position;
    private int mark=-1;
    private int limit;
    private final int capacity;
    private int remaining;

    public RingBuffer(int size, boolean direct)
    {
        this.buffer = allocate(size, direct);
        this.position = 0;
        this.mark = -1;
        this.limit = 0;
        this.capacity = buffer.capacity();
    }

    protected abstract B allocate(int size, boolean direct);
    /**
     * Returns true is there are remaining items.
     * @return 
     */
    public final boolean hasRemaining()
    {
        return remaining > 0;
    }
    /**
     * Return  the count of remaining items.
     * @return 
     */
    public final int remaining()
    {
        return remaining;
    }
    /**
     * Set mark to position.
     */
    public final void mark()
    {
        mark = position;
    }
    protected final int rawGet(boolean markIt)
    {
        int pos;
        if (hasRemaining())
        {
            if (markIt)
            {
                mark = position;
            }
            pos = position;
            position = (position+1) % capacity;
            remaining--;
            return pos;
        }
        else
        {
            throw new BufferUnderflowException();
        }
    }
    /**
     * Reads more items to buffer.
     * @param reader
     * @throws IOException 
     */
    public int read(R reader) throws IOException
    {
        Splitter<R> s = new Splitter<R>() 
        {
            @Override
            protected int op(R reader, int position, int limit) throws IOException
            {
                return read(reader, position, limit);
            }

            @Override
            protected int op(R reader, int position1, int limit1, int position2, int limit2) throws IOException
            {
                return read(reader, position1, limit1, position2, limit2);
            }
        };
     
        int lim = mark == -1 ? position : mark;
        int count = s.doIt(reader, limit, lim);
        limit = (limit+count)%capacity;
        remaining += count;
        return count;
    }
    /**
     * Reads to buffer starting from position limit-position items. Return count
     * of actual read items.
     * @param reader
     * @param position
     * @param limit
     * @return
     * @throws IOException 
     */
    protected abstract int read(R reader, int position, int limit) throws IOException;
    /**
     * Reads to buffer first starting from position1 limit1-position1 count and 
     * then continue reading at position2 limit2-position2 items. Return count 
     * of actual read items.
     * @param reader
     * @param position1
     * @param limit1
     * @param position2
     * @param limit2
     * @return
     * @throws IOException 
     */
    protected abstract int read(R reader, int position1, int limit1, int position2, int limit2) throws IOException;
    /**
     * Write buffers content from mark (included) to position (excluded)
     * @param writer
     * @throws IOException 
     */
    public int write(W writer) throws IOException
    {
        if (mark == -1)
        {
            throw new InvalidMarkException();
        }
        Splitter<W> s = new Splitter<W>() 
        {
            @Override
            protected int op(W writer, int position, int limit) throws IOException
            {
                return write(writer, position, limit);
            }

            @Override
            protected int op(W writer, int position1, int limit1, int position2, int limit2) throws IOException
            {
                return write(writer, position1, limit1, position2, limit2);
            }
        };
        int count = s.doIt(writer, mark, position);
        position = (position+count)%capacity;
        return count;
    }
    /**
     * Writes to buffer starting from position limit-position count Return count 
     * of actual written items.
     * @param writer
     * @param position
     * @param limit
     * @return
     * @throws IOException 
     */
    protected abstract int write(W writer, int position, int limit) throws IOException;
    /**
     * Writes to buffer first starting from position1 limit1-position1 count and 
     * then continue writing to position2 limit2-position2 items. Return count 
     * of actual written items.
     * @param writer
     * @param position1
     * @param limit1
     * @param position2
     * @param limit2
     * @return
     * @throws IOException 
     */
    protected abstract int write(W writer, int position1, int limit1, int position2, int limit2) throws IOException;

    @Override
    public String toString()
    {
        return "RingBuffer{" + "position=" + position + ", mark=" + mark + ", limit=" + limit + ", capacity=" + capacity + ", remaining=" + remaining + '}';
    }

    private abstract class Splitter<T>
    {
        public int doIt(T obj, int start, int end) throws IOException
        {
            int count;
            int c = buffer.capacity();
            if (start < end)
            {
                count = op(obj, start, end);
            }
            else
            {
                if (end > 0)
                {
                    count = op(obj, start, c, 0, end);
                }
                else
                {
                    count = op(obj, start, c);
                }
            }
            return count;
        }

        protected abstract int op(T obj, int position, int limit) throws IOException;
        protected abstract int op(T obj, int position1, int limit1, int position2, int limit2) throws IOException;
    }
}
