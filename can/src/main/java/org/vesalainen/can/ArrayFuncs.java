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

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ArrayFuncs
{
    /**
     * Returns IntSupplier which constructs long from byte array
     * @param offset    In bits
     * @param length    In bits
     * @param bigEndian
     * @param signed
     * @param buf
     * @return 
     */
    public static final IntSupplier getIntSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
    {
        checkBitsInt(offset, length, buf);
        IntSupplier is;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            is = getIntSupplier(arr, buf);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            is = getIntSupplier(arr, buf);
        }
        if (signed)
        {
            int shf = 32 - length;
            IntSupplier is2 = is;
            is = ()->(is2.getAsInt()<<shf)>>shf;
        }
        return is;
    }
    /**
     * Returns LongSupplier which constructs long from byte array
     * @param offset    In bits
     * @param length    In bits
     * @param bigEndian
     * @param signed
     * @param buf
     * @return 
     */
    public static final LongSupplier getLongSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
    {
        checkBitsLong(offset, length, buf);
        LongSupplier ls;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            ls = getLongSupplier(arr, buf);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            ls = getLongSupplier(arr, buf);
        }
        if (signed)
        {
            int shf = 64 - length;
            LongSupplier ls2 = ls;
            ls = ()->(ls2.getAsLong()<<shf)>>shf;
        }
        return ls;
    }
    public static final IntSupplier getIntSupplier(byte[] arr, byte... buf)
    {
        return ()->
        {
            int len = arr.length/3;
            int res = 0;
            for (int ii=0;ii<len;ii++)
            {
                byte sh = arr[3*ii+2];
                if (sh > 0)
                {
                    res |= ((buf[arr[3*ii]]&arr[3*ii+1])&0xff)<<sh;
                }
                else
                {
                    res |= ((buf[arr[3*ii]]&arr[3*ii+1])&0xff)>>>-sh;
                }
            }
            return res;
        };
    }
    public static final LongSupplier getLongSupplier(byte[] arr, byte... buf)
    {
        return ()->
        {
            int len = arr.length/3;
            long res = 0;
            for (int ii=0;ii<len;ii++)
            {
                res |= (((long)buf[arr[3*ii]]&arr[3*ii+1])&0xff)<<arr[3*ii+2];
            }
            return res;
        };
    }
    static byte[] createBigEndian(int offset, int length)
    {
        byte[] arr = new byte[dim(offset, length)*3];
        int idx = 0;
        byte shift = (byte) length;
        int off = offset;
        int len = length;
        while (len > 0)
        {
            int bo = off % 8;
            int bits = min(8 - bo, len);
            int mb = 8 - bits;
            shift -= bits;
            byte mask = (byte) ((((0xff<<mb)&0xff)>>mb)<<bo);
            arr[3*idx] = (byte) (off/8);
            arr[3*idx+1] = mask;
            arr[3*idx+2] = shift;
            off += bits;
            len -= bits;
            idx++;
        }
        return arr;
    }
    static byte[] createLittleEndian(int offset, int length)
    {
        byte[] arr = new byte[dim(offset, length)*3];
        int idx = 0;
        byte shift = 0;
        int off = offset;
        int len = length;
        while (len > 0)
        {
            int bo = off % 8;
            int bits = min(8 - bo, len);
            int mb = 8 - bits;
            byte mask = (byte) ((((0xff<<mb)&0xff)>>mb)<<bo);
            arr[3*idx] = (byte) (off/8);
            arr[3*idx+1] = mask;
            arr[3*idx+2] = (byte) (shift-bo);
            shift += bits;
            off += bits;
            len -= bits;
            idx++;
        }
        return arr;
    }
    static int dim(int offset, int length)
    {
        int d = 1;
        int om = offset % 8;
        length -= min(om == 0 ? 8 : om, length);
        d += length / 8;
        if ((length % 8) != 0)
        {
            d++;
        }
        return d;
    }
    private static void checkBitsInt(int offset, int length, byte[] buf)
    {
        if (offset < 0) 
        {
            throw new IllegalArgumentException("negative offset");
        }
        if (length < 0) 
        {
            throw new IllegalArgumentException("negative length");
        }
        if (length > 32) 
        {
            throw new IllegalArgumentException("length > 32");
        }
        if ((offset + length) / 8 > buf.length) 
        {
            throw new IllegalArgumentException("buffer overflow");
        }
    }
    private static void checkBitsLong(int offset, int length, byte[] buf)
    {
        if (offset < 0) 
        {
            throw new IllegalArgumentException("negative offset");
        }
        if (length < 0) 
        {
            throw new IllegalArgumentException("negative length");
        }
        if (length > 64) 
        {
            throw new IllegalArgumentException("length > 64");
        }
        if ((offset + length) / 8 > buf.length) 
        {
            throw new IllegalArgumentException("buffer overflow");
        }
    }
    
}
