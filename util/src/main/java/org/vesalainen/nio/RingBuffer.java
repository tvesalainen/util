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
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    protected ReentrantLock writeLock = new ReentrantLock();
    protected ReentrantLock readLock = new ReentrantLock();
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
     * Returns the count of marked items between mark and position
     * @return 
     */
    public final int marked()
    {
        return marked;
    }
    /**
     * Returns the count of bytes available for reading
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
        readLock.lock();
        try
        {
            mark = position;
            marked = 0;
            updated();
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Return item as int value at index position from mark. If mark is not set 
     * throws InvalidMarkException
     * @param index
     * @return 
     */
    public int getAt(int index)
    {
        writeLock.lock();
        try
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
        finally
        {
            writeLock.unlock();
        }
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
        readLock.lock();
        try
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
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * This method has the same effect as by calling get until <code>hasReminder()</code> returns
     * false;
     * @param markIt 
     */
    public void getAll(boolean markIt)
    {
        readLock.lock();
        try
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
        finally
        {
            readLock.unlock();
        }
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
     * pass mark or position. Return the actual items read.
     * @param reader Number of items read.
     * @return 
     * @throws IOException 
     */
    public abstract int read(R reader) throws IOException;
    
    protected <T> int read(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        readLock.lock();
        try
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
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Write buffers content from mark (included) to position (excluded)
     * @param writer
     * @return 
     * @throws IOException 
     */
    public abstract int write(W writer) throws IOException;
    /**
     * Write buffers content from mrk (included) to position (excluded)
     * @param op
     * @param splitter
     * @return 
     * @throws IOException 
     */
    protected int write(SparseBufferOperator<B> op, Splitter<SparseBufferOperator<B>> splitter) throws IOException
    {
        writeLock.lock();
        try
        {
            if (mark == -1)
            {
                return 0;
            }
            int count = splitter.split(op, mark, marked);
            return count;
        }
        finally
        {
            writeLock.unlock();
        }
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
