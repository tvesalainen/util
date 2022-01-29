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

import static java.lang.Integer.min;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ArrayFuncs
{
    /**
     * Returns String supplier for AIS @ terminated string.
     * @param offset
     * @param buf
     * @return 
     */
    public static final Supplier<String> getAisStringSupplier2(int offset, byte[] buf)
    {
        return ()->
        {
            int len = buf[offset] & 0xff; // max length
            CharSequence seq = CharSequences.getAsciiCharSequence(buf, offset, len);
            int idx = CharSequences.indexOf(seq, '@');
            if (idx != -1)
            {
                return seq.subSequence(0, idx).toString().trim();
            }
            else
            {
                return seq.toString().trim();
            }
        };
    }
    /**
     * Returns String supplier for AIS @ terminated string.
     * @param offset bytes
     * @param length bytes
     * @param buf
     * @param limitSupplier Message length
     * @return 
     */
    public static final Supplier<String> getAisStringSupplier(int offset, int length, byte[] buf, IntSupplier limitSupplier)
    {
        checkBitsString(offset, length, buf);
        return ()->
        {
            int limit = limitSupplier.getAsInt();
            int len = min(length, limit-offset);
            CharSequence seq = CharSequences.getAsciiCharSequence(buf, offset, len);
            int idx = CharSequences.indexOf(seq, '@');
            if (idx != -1)
            {
                return seq.subSequence(0, idx).toString().trim();
            }
            else
            {
                return seq.toString().trim();
            }
        };
    }
    /**
     * 
     * @param offset    in bytes
     * @param length    in bytes
     * @param buf
     * @return 
     */
    public static final Supplier<String> getZeroTerminatingStringSupplier(int offset, int length, byte... buf)
    {
        checkBitsString(offset, length, buf);
        return ()->
        {
            CharSequence seq = CharSequences.getAsciiCharSequence(buf, offset, length);
            int idx = CharSequences.indexOf(seq, (char)0);
            if (idx != -1)
            {
                return seq.subSequence(0, idx).toString();
            }
            else
            {
                return seq.toString();
            }
        };
    }
    public static final Runnable getStringWriter(int offset, int length, byte ender, Supplier<String> stringSupplier, byte... buf)
    {
        checkBitsString(offset, length, buf);
        return ()->
        {
            String string = stringSupplier.get();
            int len = min(length, string.length())-1;
            for (int ii=0;ii<len;ii++)
            {
                buf[offset+ii] = (byte) string.charAt(ii);
            }
            buf[offset+len] = ender;
        };
    }
    public static final Runnable getIntWriter(int offset, int length, boolean bigEndian, boolean signed, IntSupplier i, byte... buf)
    {
        checkBitsInt(offset, length, buf);
        Runnable r;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            r = getIntWriter(arr, i, buf);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            r = getIntWriter(arr, i, buf);
        }
        return r;
    }
    public static final Runnable getLongWriter(int offset, int length, boolean bigEndian, boolean signed, LongSupplier l, byte... buf)
    {
        checkBitsLong(offset, length, buf);
        Runnable r;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            r = getLongWriter(arr, l, buf);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            r = getLongWriter(arr, l, buf);
        }
        return r;
    }
    public static final Runnable getIntWriter(byte[] arr, IntSupplier i, byte... buf)
    {
        int len = arr.length/3;
        return ()->
        {
            int v = i.getAsInt();
            for (int ii=0;ii<len;ii++)
            {
                int ix = (int)arr[3*ii]&0xff;
                byte sh = arr[3*ii+2];
                byte ms = arr[3*ii+1];
                buf[ix] &= ~ms;
                if (sh > 0)
                {
                    buf[ix] |= (byte) ((v>>sh)&ms);
                }
                else
                {
                    buf[ix] |= (byte) ((v<<-sh)&ms);
                }
            }
        };
    }
    public static final Runnable getLongWriter(byte[] arr, LongSupplier l, byte... buf)
    {
        int len = arr.length/3;
        return ()->
        {
            long v = l.getAsLong();
            for (int ii=0;ii<len;ii++)
            {
                int ix = (int)arr[3*ii]&0xff;
                byte sh = arr[3*ii+2];
                byte ms = arr[3*ii+1];
                buf[ix] &= ~ms;
                if (sh > 0)
                {
                    buf[ix] |= (byte) ((v>>sh)&ms);
                }
                else
                {
                    buf[ix] |= (byte) ((v<<-sh)&ms);
                }
            }
        };
    }
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
        int len = arr.length/3;
        return ()->
        {
            int res = 0;
            for (int ii=0;ii<len;ii++)
            {
                byte sh = arr[3*ii+2];
                if (sh > 0)
                {
                    res |= ((buf[(int)arr[3*ii]&0xff]&arr[3*ii+1])&0xff)<<sh;
                }
                else
                {
                    res |= ((buf[(int)arr[3*ii]&0xff]&arr[3*ii+1])&0xff)>>>-sh;
                }
            }
            return res;
        };
    }
    public static final LongSupplier getLongSupplier(byte[] arr, byte... buf)
    {
        int len = arr.length/3;
        return ()->
        {
            long res = 0;
            for (int ii=0;ii<len;ii++)
            {
                byte sh = arr[3*ii+2];
                if (sh > 0)
                {
                    res |= (long)((buf[(int)arr[3*ii]&0xff]&arr[3*ii+1])&0xff)<<sh;
                }
                else
                {
                    res |= (long)((buf[(int)arr[3*ii]&0xff]&arr[3*ii+1])&0xff)>>>-sh;
                }
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
        int dim = 0;
        int off = offset;
        int len = length;
        while (len > 0)
        {
            int bo = off % 8;
            int bits = min(8 - bo, len);
            off += bits;
            len -= bits;
            dim++;
        }
        return dim;
    }
    private static void checkBitsString(int offset, int length, byte[] buf)
    {
        if (offset < 0) 
        {
            throw new IllegalArgumentException("negative offset");
        }
        if (length < 0) 
        {
            throw new IllegalArgumentException("negative length");
        }
        if ((offset + length) > buf.length) 
        {
            throw new IllegalArgumentException("buffer overflow");
        }
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
