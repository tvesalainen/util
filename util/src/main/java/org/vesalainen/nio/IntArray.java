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
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class IntArray
{
    public static IntArray getInstance(byte[] buffer, int offset, int length)
    {
        return getInstance(ByteBuffer.wrap(buffer, offset, length));
    }
    public static IntArray getInstance(ByteBuffer bb)
    {
        return new ByteArray(bb);
    }
    public static IntArray getInstance(ShortBuffer sb)
    {
        return new ShortArray(sb);
    }
    public static IntArray getInstance(IntBuffer ib)
    {
        return new InArray(ib);
    }
    public static IntArray getInstance(int size)
    {
        return new InArray(size);
    }
    public abstract int length();
    public abstract int get(int index);
    public abstract void put(int index, int value);
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
                
    }
}
