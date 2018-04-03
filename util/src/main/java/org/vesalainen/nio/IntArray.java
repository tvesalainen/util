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
package org.vesalainen.nio;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static java.nio.ByteOrder.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.vesalainen.util.function.IntBiConsumer;

/**
 * IntArray creates a view over byte, short and int arrays and buffers.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class IntArray<T extends Buffer>
{
    protected T buffer;
    
    public int length()
    {
        return buffer.limit();
    }
    /**
     * Creates IntArray backed by byte array
     * @param buffer
     * @return 
     */
    public static IntArray getInstance(byte[] buffer)
    {
        return getInstance(buffer, 0, buffer.length);
    }
    /**
     * Creates IntArray backed by byte array with given bit-count and byte-order.
     * @param buffer
     * @param bitCount
     * @param order
     * @return 
     */
    public static IntArray getInstance(byte[] buffer, int bitCount, ByteOrder order)
    {
        return getInstance(buffer, 0, buffer.length, bitCount, order);
    }
    /**
     * Creates IntArray backed by byte array
     * @param buffer
     * @param offset
     * @param length
     * @return 
     */
    public static IntArray getInstance(byte[] buffer, int offset, int length)
    {
        return getInstance(buffer, offset, length, 8, ByteOrder.BIG_ENDIAN);
    }
    /**
     * Creates IntArray backed by byte array with given bit-count and byte-order.
     * @param buffer
     * @param offset
     * @param length
     * @param bitCount
     * @param order
     * @return 
     */
    public static IntArray getInstance(byte[] buffer, int offset, int length, int bitCount, ByteOrder order)
    {
        ByteBuffer bb = ByteBuffer.wrap(buffer, offset, length).order(order);
        switch (bitCount)
        {
            case 8:
                return getInstance(bb);
            case 16:
                return getInstance(bb.asShortBuffer());
            case 32:
                return getInstance(bb.asIntBuffer());
            default:
                throw new UnsupportedOperationException(bitCount+" not supported");
        }
    }
    /**
     * Creates IntArray backed by short array
     * @param buffer
     * @return 
     */
    public static IntArray getInstance(short[] buffer)
    {
        return getInstance(buffer, 0, buffer.length);
    }
    /**
     * Creates IntArray backed by short array
     * @param buffer
     * @param offset
     * @param length
     * @return 
     */
    public static IntArray getInstance(short[] buffer, int offset, int length)
    {
        return getInstance(ShortBuffer.wrap(buffer, offset, length));
    }
    /**
     * Creates IntArray backed by int array
     * @param buffer
     * @return 
     */
    public static IntArray getInstance(int[] buffer)
    {
        return getInstance(buffer, 0, buffer.length);
    }
    /**
     * Creates IntArray backed by int array
     * @param buffer
     * @param offset
     * @param length
     * @return 
     */
    public static IntArray getInstance(int[] buffer, int offset, int length)
    {
        return getInstance(IntBuffer.wrap(buffer, offset, length));
    }
    /**
     * Creates IntArray backed by ByteBuffer
     * @param bb
     * @return 
     */
    public static IntArray getInstance(ByteBuffer bb)
    {
        return getInstance(bb, 8, bb.order());
    }
    public static IntArray getInstance(ByteBuffer bb, int bitCount, ByteOrder order)
    {
        bb.order(order);
        switch (bitCount)
        {
            case 8:
                return new ByteArray(bb);
            case 16:
                return new ShortArray(bb.asShortBuffer());
            case 32:
                return new InArray(bb.asIntBuffer());
            default:
                throw new UnsupportedOperationException(bitCount+" not supported");
        }
    }
    /**
     * Creates IntArray backed by ShortBuffer
     * @param sb
     * @return 
     */
    public static IntArray getInstance(ShortBuffer sb)
    {
        return new ShortArray(sb);
    }
    /**
     * Creates IntArray backed by IntBuffer
     * @param ib
     * @return 
     */
    public static IntArray getInstance(IntBuffer ib)
    {
        return new InArray(ib);
    }
    /**
     * Creates IntArray of size length.
     * @param size
     * @return 
     */
    public static IntArray getInstance(int size)
    {
        return getInstance(size, 32, BIG_ENDIAN);
    }
    /**
     * Creates IntArray of given length, bitCount and byte-order
     * @param size
     * @param bitCount
     * @param order
     * @return 
     */
    public static IntArray getInstance(int size, int bitCount, ByteOrder order)
    {
        switch (bitCount)
        {
            case 8:
                return getInstance(new byte[size], bitCount, order);
            case 16:
                return getInstance(new byte[2*size], bitCount, order);
            case 32:
                return new InArray(size);
            default:
                throw new UnsupportedOperationException(bitCount+" not supported");
        }
    }
    /**
     * Returns value at index.
     * @param index
     * @return 
     */
    public abstract int get(int index);
    /**
     * Puts value at index.
     * @param index
     * @param value 
     */
    public abstract void put(int index, int value);
    /**
     * Returns bit-count of underlying buffer.
     * @return 
     */
    public abstract int getBits();
    /**
     * Returns byte count of underlying buffer.
     * @return 
     */
    public abstract int getBytes();
    /**
     * Returns the maximum possible value of this IntArray. Not the current max value.
     * @return 
     */
    public abstract int getMaxPossibleValue();
    /**
     * Returns the minimum possible value of this IntArray. Not the current min value.
     * @return 
     */
    public abstract int getMinPossibleValue();
    /**
     * Copies given IntArrays values to this. Throws IllegalArgumentException
     * if lengths are not same.
     * @param to 
     */
    public void copy(IntArray to)
    {
        if (length() != to.length())
        {
            throw new IllegalArgumentException("array not same size");
        }
        if (buffer.hasArray() && to.buffer.hasArray() && 
                buffer.array().getClass().getComponentType() == to.buffer.array().getClass().getComponentType())
        {
            System.arraycopy(to.buffer.array(), 0, buffer.array(), 0, length());
        }
        else
        {
            int len = length();
            for (int ii=0;ii<len;ii++)
            {
                put(ii, to.get(ii));
            }
        }
    }
    /**
     * Calls consumer for each index and value
     * @param consumer 
     */
    public void forEach(IntBiConsumer consumer)
    {
        int len = length();
        for (int ii=0;ii<len;ii++)
        {
            consumer.accept(ii, get(ii));
        }
    }
    public void fill(double[] array)
    {
        if (array.length != length())
        {
            throw new IllegalArgumentException("illegal length");
        }
        int len = length();
        for (int ii=0;ii<len;ii++)
        {
            array[ii] = get(ii);
        }
    }
    /**
     * Returns stream of IntArrays contents.
     * @return 
     */
    public IntStream stream()
    {
        return StreamSupport.intStream(spliterator(), false);
    }
    /**
     * Returns spliterator of IntArrays contents.
     * @return 
     */
    public Spliterator.OfInt spliterator()
    {
        return Spliterators.spliterator(iterator(), length(), 0);
    }
    /**
     * Returns iterator of IntArrays contents.
     * @return 
     */
    public PrimitiveIterator.OfInt iterator()
    {
        return new IntIterator();
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.buffer);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final IntArray<?> other = (IntArray<?>) obj;
        if (!Objects.equals(this.buffer, other.buffer))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "IntArray{" + buffer + '}';
    }
    
    public static class ByteArray extends IntArray<ByteBuffer>
    {

        public ByteArray(ByteBuffer bb)
        {
            this.buffer = bb;
        }
        @Override
        public int get(int index)
        {
            return buffer.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            buffer.put(index, (byte) value);
        }

        @Override
        public int getBits()
        {
            return 8;
        }

        @Override
        public int getBytes()
        {
            return 1;
        }

        @Override
        public int getMaxPossibleValue()
        {
            return Byte.MAX_VALUE;
        }

        @Override
        public int getMinPossibleValue()
        {
            return Byte.MIN_VALUE;
        }
                
    }
    public static class ShortArray extends IntArray<ShortBuffer>
    {

        public ShortArray(ShortBuffer bb)
        {
            this.buffer = bb;
        }
        @Override
        public int get(int index)
        {
            return buffer.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            buffer.put(index, (short) value);
        }

        @Override
        public int getBits()
        {
            return 16;
        }

        @Override
        public int getBytes()
        {
            return 2;
        }

        @Override
        public int getMaxPossibleValue()
        {
            return Short.MAX_VALUE;
        }

        @Override
        public int getMinPossibleValue()
        {
            return Short.MIN_VALUE;
        }
                
    }
    public static class InArray extends IntArray<IntBuffer>
    {

        public InArray(int size)
        {
            this(IntBuffer.allocate(size));
        }

        public InArray(IntBuffer ib)
        {
            this.buffer = ib;
        }
        @Override
        public int get(int index)
        {
            return buffer.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            buffer.put(index, value);
        }

        @Override
        public int getBits()
        {
            return 32;
        }

        @Override
        public int getBytes()
        {
            return 4;
        }

        @Override
        public int getMaxPossibleValue()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMinPossibleValue()
        {
            return Integer.MIN_VALUE;
        }
                
    }
    public class IntIterator implements PrimitiveIterator.OfInt
    {
        private int index;
        @Override
        public int nextInt()
        {
            return get(index++);
        }

        @Override
        public void forEachRemaining(IntConsumer action)
        {
            int length = length();
            while (index < length)
            {
                action.accept(get(index++));
            }
        }

        @Override
        public boolean hasNext()
        {
            return index < length();
        }

        @Override
        public Integer next()
        {
            return get(index++);
        }
        
    }
}
