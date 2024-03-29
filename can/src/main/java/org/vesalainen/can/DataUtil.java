/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.vesalainen.nio.ReadBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class DataUtil
{
    public static int length(CharSequence data)
    {
        int length = data.length();
        if (length % 2 != 0)
        {
            throw new IllegalArgumentException("length not even");
        }
        return length/2;
    }
    public static long asLong(CharSequence data)
    {
        long res = 0;
        int length = length(data);
        if (length > 8)
        {
            throw new IllegalArgumentException("data doesn't fit in long");
        }
        int len = data.length();
        for (int ii=0;ii<len;ii++)
        {
            long h = fromHex(data.charAt(ii));
            res |= h<<(4*(ii+1-2*(ii%2)));
            
        }
        return res;
    }
    public static long asLong(byte... data)
    {
        int length = data.length;
        if (length > 8)
        {
            throw new IllegalArgumentException("data doesn't fit in long");
        }
        long res = 0;
        for (int ii=0;ii<length;ii++)
        {
            long h = data[ii]&0xff;
            res |= h<<(8*ii);
            
        }
        return res;
    }
    public static long asLong(byte length, ByteBuffer bb)
    {
        if (length > 8)
        {
            throw new IllegalArgumentException("data doesn't fit in long");
        }
        long res = 0;
        for (int ii=0;ii<length;ii++)
        {
            long h = bb.get()&0xff;
            res |= h<<(8*ii);
            
        }
        return res;
    }
    public static void fromLong(long v, byte[] buf, int off, int len)
    {
        fromLong(v, 0, buf, off, len);
    }
    public static void fromLong(long v, int sof, byte[] buf, int off, int len)
    {
        for (int ii=0;ii<len;ii++)
        {
            int sht = (ii+sof)*8;
            buf[ii+off] = (byte) ((v & (0xffL<<sht))>>>sht);
        }
    }
    public static int get(long v, int index)
    {
        int sht = index*8;
        return (int) (((v & (0xffL<<sht))>>>sht) & 0xff);
    }
    public static long set(long v, int index, long i)
    {
        int sht = index*8;
        v |= ((i & 0xff)<<sht);
        return v;
    }
    public static String toString(long v, int len)
    {
        StringBuilder sb = new StringBuilder();
        append(v, len, sb);
        return sb.toString();
    }
    public static void append(long v, int len, Appendable o)
    {
        for (int ii=0;ii<len;ii++)
        {
            try
            {
                int sht = (ii)*8;
                o.append(String.format("%x ", (v & (0xffL<<sht))>>>sht));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
    private static int fromHex(char cc)
    {
        switch (cc)
        {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                throw new IllegalArgumentException(cc+" not hex");
        }
    }

    public static class LongBuffer implements ReadBuffer
    {
        private long data;
        private int length;
        private int index;

        public LongBuffer(long data, int length)
        {
            if (length > 8 || length < 0)
            {
                throw new UnsupportedOperationException(length+" length not supported");
            }
            this.data = data;
            this.length = length;
        }
        
        @Override
        public int remaining()
        {
            return length - index;
        }

        @Override
        public byte get()
        {
            return (byte) DataUtil.get(data, index++);
        }
        
    }
}
