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
import java.nio.InvalidMarkException;

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
 *   free|                        |free---
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
    /**
     * Creates a RingBuffer
     * @param buffer 
     * @see java.nio.Buffer
     */
    protected RingBuffer(B buffer)
    {
        this.buffer = buffer;
        this.position = 0;
        this.mark = -1;
        this.limit = 0;
        this.capacity = buffer.capacity();
     
    }

    /**
     * Returns true is there are remaining items between position and limit.
     * @return 
     */
    public final boolean hasRemaining()
    {
        return remaining > 0;
    }
    /**
     * Returns true if there are not any space in buffer to fill in.
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
     * Returns the count of marked items between mark and position
     * @return 
     */
    public final int marked()
    {
        return marked;
    }
    /**
     * Returns the count of bytes available for filling
     * @return 
     */
    public final int free()
    {
        return capacity-marked-remaining;
    }
    /**
     * Set mark to position.
     */
    public final void mark()
    {
        mark = position;
        marked = 0;
        updated();
    }
    /**
     * Return item as int value at index position from mark. If mark is not set 
     * throws InvalidMarkException
     * @param index
     * @return 
     */
    public int getAt(int index)
    {
        if (mark == -1)
        {
            throw new InvalidMarkException();
        }
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
            updated();
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
        updated();
    }
    /**
     * This method is called after some parameters like marked are updated.
     */
    protected void updated()
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
 pass mark or position. Return the actual items fill.
     * @param reader Number of items fill.
     * @return 
     * @throws IOException 
     */
    public abstract int fill(R reader) throws IOException;
    /**
     * @deprecated Use fill
     * @param reader
     * @return
     * @throws IOException 
     */
    public int read(R reader) throws IOException
    {
        return RingBuffer.this.fill(reader);
    }
    
    protected <T> int fill(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        int count = splitter.split(op, limit, free());
        if (count != -1)
        {
            limit = (limit+count)%capacity;
            remaining += count;
        }
        updated();
        return count;
    }
    /**
     * @deprecated Use fill
     * @param <T>
     * @param op
     * @param splitter
     * @return
     * @throws IOException 
     */
    protected <T> int read(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        return fill(op, splitter);
    }
    /**
     * Write buffers content from mark (included) to position (excluded)
     * @param writer
     * @return 
     * @throws IOException 
     */
    public abstract int writeTo(W writer) throws IOException;
    /**
     * @deprecated Use writeTo
     * @param writer
     * @return
     * @throws IOException 
     */
    public int write(W writer) throws IOException
    {
        return RingBuffer.this.writeTo(writer);
    }
    /**
     * Write buffers content from mrk (included) to position (excluded)
     * @param op
     * @param splitter
     * @return 
     * @throws IOException 
     */
    protected int writeTo(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        if (mark == -1)
        {
            return 0;
        }
        int count = splitter.split(op, mark, marked);
        return count;
    }
    /**
     * @deprecated Use writeTo
     * @param op
     * @param splitter
     * @return
     * @throws IOException 
     */
    protected int write(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        return writeTo(op, splitter);
    }
    /**
     * Returns the current position. Only use for this position is in marked writeTo method.
     * @return 
     * @see org.vesalainen.nio.RingByteBuffer#write(java.lang.Object, int) 
     */
    public int getPosition()
    {
        return position;
    }
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

}
