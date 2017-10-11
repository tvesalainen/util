/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 * A RingBuffer wrapper for Buffer. 
 * <p>This class acts also as CharSequence between mark and position.
 * <p>
 * Example:
 * <pre>
 *   AAAAMMMMMMMMMMMMMRRRRRRRRRRRRAAAAAAAA
 *       ^            ^           ^
 *       |            |           limit
 *       |            position
 *       mark
 *       |--marked----|-remaining-|
 *       |CharSequence|
 * </pre>
 *  A bytes are available for reading.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <B> Buffer type
 * @param <R> Reader type
 * @param <W> Writer type
 */
public abstract class RingBuffer<B extends Buffer,R,W> implements CharSequence
{
    protected final B buffer;
    protected int position;
    protected int mark=-1;
    protected int limit;
    protected final int capacity;
    protected int remaining;
    protected int marked;
    private final Splitter<R> readSplitter;
    private final Splitter<W> writeSplitter;
    /**
     * Creates a RingBuffer of size. The backing buffer is either direct- or 
     * heapbuffer depending on direct parameter.
     * <p>Method names which are same as in java.nio.Buffer have the same meaning.
     * Reading and writing differs from Buffer.
     * @param size
     * @param direct 
     * @see java.nio.Buffer
     */
    public RingBuffer(int size, boolean direct)
    {
        this.buffer = allocate(size, direct);
        this.position = 0;
        this.mark = -1;
        this.limit = 0;
        this.capacity = buffer.capacity();
        this.readSplitter = new Splitter<R>() 
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
        this.writeSplitter = new Splitter<W>() 
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
     
    }

    protected abstract B allocate(int size, boolean direct);
    /**
     * Returns true is there are remaining items between position and limit.
     * @return 
     */
    public final boolean hasRemaining()
    {
        return remaining > 0;
    }
    /**
     * Returns true if there are not any space in buffer to read in.
     * @return 
     */
    public boolean isFull()
    {
        return marked+remaining==capacity;
    }
    /**
     * Return  the count of remaining items between position and limit.
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
            marked = 0;
    }
    /**
     * Return item as int value at index position from mark. If mark is not set 
     * throws InvalidMarkException
     * @param index
     * @return 
     */
    public int getAt(int index)
    {
        if (index > marked)
        {
            throw new IndexOutOfBoundsException();
        }
        return implGetAt(index);
    }
    public abstract int implGetAt(int index);
    /**
     * Returns the current position and increments it. If markIt == true the 
     * returned position is marked.
     * <p>This method is a support for concrete subclasses get method.
     * @param markIt
     * @return 
     */
    protected final int rawGet(boolean markIt)
    {
        int pos;
        if (hasRemaining())
        {
            if (markIt)
            {
                mark = position;
                marked = 1;
            }
            else
            {
                marked++;
            }
            pos = position;
            position = (position+1) % capacity;
            remaining--;
            check();
            return pos;
        }
        else
        {
            throw new BufferUnderflowException();
        }
    }
    /**
     * This method has the same effect as by calling get until <code>hasReminder()</code> returns
     * false;
     * @param markIt 
     */
    public void getAll(boolean markIt)
    {
        if (markIt)
        {
            mark = position;
            marked = remaining;
        }
        else
        {
            marked += remaining;
        }
        position = (position+remaining) % capacity;
        remaining = 0;
        check();
    }
    private void check()
    {
        if (remaining < 0 && remaining > capacity)
        {
            throw new IllegalArgumentException(remaining+" illegal remaining at %s"+toString());
        }
        if (marked < 0 && marked > capacity)
        {
            throw new IllegalArgumentException(marked+" illegal marked at %s"+toString());
        }
    }
    /**
     * Reads more items to buffer between limit and mark/position. Read will not
     * pass mark or position. Return the actual items read.
     * @param reader Number of items read.
     * @return 
     * @throws IOException 
     */
    public int read(R reader) throws IOException
    {
        int lim = mark == -1 ? position : mark;
        int count = readSplitter.doIt(reader, limit, lim);
        if (count != -1)
        {
            limit = (limit+count)%capacity;
            remaining += count;
        }
        check();
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
     * @return 
     * @throws IOException 
     */
    public int write(W writer) throws IOException
    {
        return write(writer, mark);
    }
    /**
     * Write buffers content from mrk (included) to position (excluded)
     * @param writer
     * @param mrk
     * @return 
     * @throws IOException 
     */
    public int write(W writer, int mrk) throws IOException
    {
        if (mrk == -1)
        {
            return 0;
        }
        int count = writeSplitter.doIt(writer, mrk, position);
        return count;
    }
    /**
     * Returns the current position. Only use for this position is in marked write method.
     * @return 
     * @see org.vesalainen.nio.RingByteBuffer#write(java.lang.Object, int) 
     */
    public int getPosition()
    {
        return position;
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
    /**
     * Returns input between mark and position as a string
     * @return 
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<marked;ii++)
        {
            sb.append(charAt(ii));
        }
        return sb.toString();
    }
    
    @Override
    public String toString()
    {
        return "RingBuffer{" + "m=" + mark + ", p=" + position + ", l=" + limit + ", c=" + capacity + ", r=" + remaining + '}';
    }

    @Override
    public int length()
    {
        return marked;
    }

    @Override
    public char charAt(int index)
    {
        return (char) getAt(index);
    }
    /**
     * Note that implementation uses StringBuilder to create String as CharSequence.
     * @param start
     * @param end
     * @return 
     */
    @Override
    public CharSequence subSequence(int start, int end)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=start;ii<end;ii++)
        {
            sb.append((char)getAt(ii));
        }
        return sb.toString();
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
