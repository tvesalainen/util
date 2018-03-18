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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
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
public abstract class IntArray
{
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
        return new ByteArray(bb);
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
        return new InArray(size);
    }
    /**
     * Returns length of IntArray
     * @return 
     */
    public abstract int length();
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
        int len = length();
        for (int ii=0;ii<len;ii++)
        {
            to.put(ii, get(ii));
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
    public static class ByteArray extends IntArray
    {
        private ByteBuffer bb;

        public ByteArray(ByteBuffer bb)
        {
            this.bb = bb;
        }
        @Override
        public int length()
        {
            return bb.limit();
        }

        @Override
        public int get(int index)
        {
            return bb.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            bb.put(index, (byte) value);
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
    public static class ShortArray extends IntArray
    {
        private ShortBuffer sb;

        public ShortArray(ShortBuffer bb)
        {
            this.sb = bb;
        }
        @Override
        public int length()
        {
            return sb.limit();
        }

        @Override
        public int get(int index)
        {
            return sb.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            sb.put(index, (short) value);
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
    public static class InArray extends IntArray
    {
        private IntBuffer ib;

        public InArray(int size)
        {
            this(IntBuffer.allocate(size));
        }

        public InArray(IntBuffer ib)
        {
            this.ib = ib;
        }
        @Override
        public int length()
        {
            return ib.limit();
        }

        @Override
        public int get(int index)
        {
            return ib.get(index);
        }

        @Override
        public void put(int index, int value)
        {
            ib.put(index, value);
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
