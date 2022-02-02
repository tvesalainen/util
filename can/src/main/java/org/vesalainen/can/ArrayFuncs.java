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
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.SIGNED;
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
     * @return 
     */
    public static final Function<byte[],String> getAisStringFunction2(int offset)
    {
        return (buf)->
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
     * @param limitSupplier Message length
     * @return 
     */
    public static final Function<byte[],String> getAisStringFunction(int offset, int length, IntSupplier limitSupplier)
    {
        checkBitsString(offset, length);
        return (buf)->
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
     * @return 
     */
    public static final Function<byte[],String> getZeroTerminatingStringFunction(int offset, int length)
    {
        checkBitsString(offset, length);
        return (buf)->
        {
            CharSequence seq = CharSequences.getAsciiCharSequence(buf, offset, length);
            int idx = CharSequences.indexOf(seq, (cc)->cc<' '|| cc>127);
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
    public static final Consumer<byte[]> getStringWriter(int offset, int length, byte ender, Supplier<String> stringSupplier)
    {
        checkBitsString(offset, length);
        return (buf)->
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
    public static final Consumer<byte[]> getIntWriter(int offset, int length, boolean bigEndian, boolean signed, IntSupplier i)
    {
        checkBitsInt(offset, length);
        Consumer<byte[]> r;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            r = getIntWriter(arr, i);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            r = getIntWriter(arr, i);
        }
        return r;
    }
    public static final Consumer<byte[]> getLongWriter(int offset, int length, boolean bigEndian, boolean signed, LongSupplier l)
    {
        checkBitsLong(offset, length);
        Consumer<byte[]> r;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            r = getLongWriter(arr, l);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            r = getLongWriter(arr, l);
        }
        return r;
    }
    public static final Consumer<byte[]> getIntWriter(byte[] arr, IntSupplier i)
    {
        int len = arr.length/3;
        return (buf)->
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
    public static final Consumer<byte[]> getLongWriter(byte[] arr, LongSupplier l)
    {
        int len = arr.length/3;
        return (buf)->
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
    public static final ToIntFunction<byte[]> getIntFunction(SignalClass sc, int off)
    {
        return getIntFunction(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED);
    }
    public static final ToLongFunction<byte[]> getLongFunction(SignalClass sc, int off)
    {
        return getLongFunction(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED);
    }
    public static final Function<byte[],String> getZeroTerminatingStringFunction(SignalClass sc, int off)
    {
        return ArrayFuncs.getZeroTerminatingStringFunction((sc.getStartBit()+off)/8, sc.getSize()/8);
    }
    public static final Function<byte[],String> getAisStringFunction(SignalClass sc, int off, IntSupplier currentBytesSupplier, Supplier<byte[]> arraySupplier)
    {
        return ArrayFuncs.getAisStringFunction((sc.getStartBit()+off)/8, sc.getSize()/8, currentBytesSupplier);
    }
    public static final Function<byte[],String> getAisStringFunction2(SignalClass sc, int off)
    {
        return ArrayFuncs.getAisStringFunction2((sc.getStartBit()+off)/8);
    }
    public static final ToIntFunction<byte[]> getIntSupplier(SignalClass sc, int off, Supplier<byte[]> arraySupplier)
    {
        return getIntFunction(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED);
    }
    public static final Function<byte[],String> getAisStringFunction(SignalClass sc, int off, IntSupplier currentBytesSupplier)
    {
        return ArrayFuncs.getAisStringFunction((sc.getStartBit()+off)/8, sc.getSize()/8, currentBytesSupplier);
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
    public static final ToIntFunction<byte[]> getIntFunction(int offset, int length, boolean bigEndian, boolean signed)
    {
        checkBitsInt(offset, length);
        ToIntFunction<byte[]> is;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            is = getIntFunction(arr);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            is = getIntFunction(arr);
        }
        if (signed)
        {
            int shf = 32 - length;
            ToIntFunction<byte[]> is2 = is;
            is = (buf)->(is2.applyAsInt(buf)<<shf)>>shf;
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
    public static final ToLongFunction<byte[]> getLongFunction(int offset, int length, boolean bigEndian, boolean signed)
    {
        checkBitsLong(offset, length);
        ToLongFunction<byte[]>  ls;
        if (bigEndian)
        {
            byte[] arr = createBigEndian(offset, length);
            ls = getLongFunction(arr);
        }
        else
        {
            byte[] arr = createLittleEndian(offset, length);
            ls = getLongFunction(arr);
        }
        if (signed)
        {
            int shf = 64 - length;
            ToLongFunction<byte[]>  ls2 = ls;
            ls = (buf)->(ls2.applyAsLong(buf)<<shf)>>shf;
        }
        return ls;
    }
    private static final ToIntFunction<byte[]> getIntFunction(byte[] arr)
    {
        int len = arr.length/3;
        return (buf)->
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
    private static final ToLongFunction<byte[]> getLongFunction(byte[] arr)
    {
        int len = arr.length/3;
        return (buf)->
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
    private static byte[] createBigEndian(int offset, int length)
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
    private static byte[] createLittleEndian(int offset, int length)
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
    private static int dim(int offset, int length)
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
    private static void checkBitsString(int offset, int length)
    {
        if (offset < 0) 
        {
            throw new IllegalArgumentException("negative offset");
        }
        if (length < 0) 
        {
            throw new IllegalArgumentException("negative length");
        }
    }
    private static void checkBitsInt(int offset, int length)
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
    }
    private static void checkBitsLong(int offset, int length)
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
    }
    
}
