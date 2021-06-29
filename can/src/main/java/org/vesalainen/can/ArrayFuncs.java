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
        boolean aligned8 = (offset % 8) == 0 && (length % 8) == 0;
        if (aligned8)
        {
            return getAlignedIntSupplier(offset, length, bigEndian, signed, buf);
        }
        else
        {
            return getIntSupplierX(offset, length, bigEndian, signed, buf);
        }
        
    }
    static final IntSupplier getAlignedIntSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
    {
            int off = offset/8;
            int len = length/8;
            if (signed)
            {
                if (bigEndian)
                {
                    return getIntSignedBigEndian(off, len, buf);
                }
                else
                {
                    return getIntSignedLittleEndian(off, len, buf);
                }
            }
            else
            {
                if (length > 63)
                {
                    throw new IllegalArgumentException("length > 63 for unsigned");
                }
                if (bigEndian)
                {
                    return getIntUnsignedBigEndian(off, len, buf);
                }
                else
                {
                    return getIntUnsignedLittleEndian(off, len, buf);
                }
            }
    }
    static IntSupplier getIntSupplierX(int offset, int length, boolean bigEndian, boolean signed, byte[] buf)
    {
        if (signed)
        {
            if (bigEndian)
            {
                return getIntSignedBigEndianX(offset, length, buf);
            }
            else
            {
                return getIntSignedLittleEndianX(offset, length, buf);
            }
        }
        else
        {
            if (length > 63)
            {
                throw new IllegalArgumentException("length > 63 for unsigned");
            }
            if (bigEndian)
            {
                return getIntUnsignedBigEndianX(offset, length, buf);
            }
            else
            {
                return getIntUnsignedLittleEndianX(offset, length, buf);
            }
        }
    }
    static final IntSupplier getIntUnsignedLittleEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int res = 0;
            int start = offset+length-min(8, length);
            for (int ii=0;ii<length;ii++)
            {
                int jj = start+(ii%8)-8*(ii/8);
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final IntSupplier getIntSignedLittleEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int start = offset+length-min(8, length);
            int jj = start;
            int res = (buf[jj/8]>>(7-jj%8) & 0x1) == 1 ? -1 : 0;
            for (int ii=1;ii<length;ii++)
            {
                jj = start+(ii%8)-8*(ii/8);
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final IntSupplier getIntUnsignedBigEndianX(int offset, int length, byte... buf)
    {
        if (length > 31)
        {
            throw new IllegalArgumentException("length > 31 for unsigned");
        }
        return ()->
        {
            int res = 0;
            for (int ii=0;ii<length;ii++)
            {
                int jj = ii+offset;
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final IntSupplier getIntSignedBigEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int res = (buf[offset/8]>>(7-offset%8) & 0x1) == 1 ? -1 : 0;
            for (int ii=1;ii<length;ii++)
            {
                int jj = ii+offset;
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final IntSupplier getIntSignedBigEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off];
            case 2:
                return ()-> (buf[off]<<8) + 
                            (buf[off+1] & 0xff);
            case 3:
                return ()-> (buf[off]<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off+2] & 0xff);
            case 4:
                return ()-> (buf[off]<<24) + 
                            ((buf[off+1] & 0xff)<<16) + 
                            ((buf[off+2] & 0xff)<<8) + 
                            (buf[off+3] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final IntSupplier getIntUnsignedBigEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off] & 0xff;
            case 2:
                return ()-> (((buf[off] & 0xff)<<8)) + 
                            (buf[off+1] & 0xff);
            case 3:
                return ()-> ((buf[off] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off+2] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final IntSupplier getIntSignedLittleEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off];
            case 2:
                return ()-> (buf[off+1]<<8) + 
                            (buf[off] & 0xff);
            case 3:
                return ()-> (buf[off+2]<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            case 4:
                return ()-> (buf[off+3]<<24) + 
                            ((buf[off+2] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final IntSupplier getIntUnsignedLittleEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off] & 0xff;
            case 2:
                return ()-> (((buf[off+1] & 0xff)<<8)) + 
                            (buf[off] & 0xff);
            case 3:
                return ()-> ((buf[off+2] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
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
    /*------------------------------------------------------------------------------*/
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
        boolean aligned8 = (offset % 8) == 0 && (length % 8) == 0;
        if (aligned8)
        {
            return getAlignedLongSupplier(offset, length, bigEndian, signed, buf);
        }
        else
        {
            return getLongSupplierX(offset, length, bigEndian, signed, buf);
        }
        
    }
    static final LongSupplier getAlignedLongSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
    {
            int off = offset/8;
            int len = length/8;
            if (signed)
            {
                if (bigEndian)
                {
                    return getLongSignedBigEndian(off, len, buf);
                }
                else
                {
                    return getLongSignedLittleEndian(off, len, buf);
                }
            }
            else
            {
                if (length > 63)
                {
                    throw new IllegalArgumentException("length > 63 for unsigned");
                }
                if (bigEndian)
                {
                    return getLongUnsignedBigEndian(off, len, buf);
                }
                else
                {
                    return getLongUnsignedLittleEndian(off, len, buf);
                }
            }
    }
    static LongSupplier getLongSupplierX(int offset, int length, boolean bigEndian, boolean signed, byte[] buf)
    {
        if (signed)
        {
            if (bigEndian)
            {
                return getLongSignedBigEndianX(offset, length, buf);
            }
            else
            {
                return getLongSignedLittleEndianX(offset, length, buf);
            }
        }
        else
        {
            if (length > 63)
            {
                throw new IllegalArgumentException("length > 63 for unsigned");
            }
            if (bigEndian)
            {
                return getLongUnsignedBigEndianX(offset, length, buf);
            }
            else
            {
                return getLongUnsignedLittleEndianX(offset, length, buf);
            }
        }
    }
    static final LongSupplier getLongUnsignedLittleEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int start = offset+length-min(8, length);
            long res = 0;
            for (int ii=0;ii<length;ii++)
            {
                int jj = start+(ii%8)-8*(ii/8);
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final LongSupplier getLongSignedLittleEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int start = offset+length-min(8, length);
            int jj = start;
            long res = (buf[jj/8]>>(7-jj%8) & 0x1) == 1 ? -1 : 0;
            for (int ii=1;ii<length;ii++)
            {
                jj = start+(ii%8)-8*(ii/8);
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final LongSupplier getLongUnsignedBigEndianX(int offset, int length, byte... buf)
    {
        if (length > 63)
        {
            throw new IllegalArgumentException("length > 63 for unsigned");
        }
        return ()->
        {
            long res = 0;
            for (int ii=0;ii<length;ii++)
            {
                int jj = ii+offset;
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final LongSupplier getLongSignedBigEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            long res = (buf[offset/8]>>(7-offset%8) & 0x1) == 1 ? -1 : 0;
            for (int ii=1;ii<length;ii++)
            {
                int jj = ii+offset;
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final LongSupplier getLongSignedBigEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off];
            case 2:
                return ()-> (buf[off]<<8) + 
                            (buf[off+1] & 0xff);
            case 3:
                return ()-> (buf[off]<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off+2] & 0xff);
            case 4:
                return ()-> (buf[off]<<24) + 
                            ((buf[off+1] & 0xff)<<16) + 
                            ((buf[off+2] & 0xff)<<8) + 
                            (buf[off+3] & 0xff);
            case 5:
                return ()-> ((long)buf[off]<<32) + 
                            ((long)(buf[off+1] & 0xff)<<24) + 
                            ((long)(buf[off+2] & 0xff)<<16) + 
                            ((long)(buf[off+3] & 0xff)<<8) + 
                            ((long)buf[off+4] & 0xff);
            case 6:
                return ()-> ((long)buf[off]<<40) + 
                            (((long)buf[off+1] & 0xff)<<32) + 
                            (((long)buf[off+2] & 0xff)<<24) + 
                            (((long)buf[off+3] & 0xff)<<16) + 
                            (((long)buf[off+4] & 0xff)<<8) + 
                            ((long)buf[off+5] & 0xff);
            case 7:
                return ()-> ((long)buf[off]<<48) + 
                            (((long)buf[off+1] & 0xff)<<40) + 
                            (((long)buf[off+2] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+4] & 0xff)<<16) + 
                            (((long)buf[off+5] & 0xff)<<8) + 
                            ((long)buf[off+6] & 0xff);
            case 8:
                return ()-> ((long)buf[off]<<56) + 
                            (((long)buf[off+1] & 0xff)<<48) + 
                            (((long)buf[off+2] & 0xff)<<40) + 
                            (((long)buf[off+3] & 0xff)<<32) + 
                            (((long)buf[off+4] & 0xff)<<24) + 
                            (((long)buf[off+5] & 0xff)<<16) + 
                            (((long)buf[off+6] & 0xff)<<8) + 
                            ((long)buf[off+7] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final LongSupplier getLongUnsignedBigEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off] & 0xff;
            case 2:
                return ()-> (((buf[off] & 0xff)<<8)) + 
                            (buf[off+1] & 0xff);
            case 3:
                return ()-> ((buf[off] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off+2] & 0xff);
            case 4:
                return ()-> (((long)buf[off] & 0xff)<<24) + 
                            (((long)buf[off+1] & 0xff)<<16) + 
                            (((long)buf[off+2] & 0xff)<<8) + 
                            ((long)buf[off+3] & 0xff);
            case 5:
                return ()-> (((long)buf[off] & 0xff)<<32) + 
                            ((long)(buf[off+1] & 0xff)<<24) + 
                            ((long)(buf[off+2] & 0xff)<<16) + 
                            ((long)(buf[off+3] & 0xff)<<8) + 
                            ((long)buf[off+4] & 0xff);
            case 6:
                return ()-> (((long)buf[off] & 0xff)<<40) + 
                            (((long)buf[off+1] & 0xff)<<32) + 
                            (((long)buf[off+2] & 0xff)<<24) + 
                            (((long)buf[off+3] & 0xff)<<16) + 
                            (((long)buf[off+4] & 0xff)<<8) + 
                            ((long)buf[off+5] & 0xff);
            case 7:
                return ()-> (((long)buf[off] & 0xff)<<48) + 
                            (((long)buf[off+1] & 0xff)<<40) + 
                            (((long)buf[off+2] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+4] & 0xff)<<16) + 
                            (((long)buf[off+5] & 0xff)<<8) + 
                            ((long)buf[off+6] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final LongSupplier getLongSignedLittleEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off];
            case 2:
                return ()-> (buf[off+1]<<8) + 
                            (buf[off] & 0xff);
            case 3:
                return ()-> (buf[off+2]<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            case 4:
                return ()-> (buf[off+3]<<24) + 
                            ((buf[off+2] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            case 5:
                return ()-> ((long)buf[off+4]<<32) + 
                            ((long)(buf[off+3] & 0xff)<<24) + 
                            ((long)(buf[off+2] & 0xff)<<16) + 
                            ((long)(buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 6:
                return ()-> ((long)buf[off+5]<<40) + 
                            (((long)buf[off+4] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 7:
                return ()-> ((long)buf[off+6]<<48) + 
                            (((long)buf[off+5] & 0xff)<<40) + 
                            (((long)buf[off+4] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 8:
                return ()-> ((long)buf[off+7]<<56) + 
                            (((long)buf[off+6] & 0xff)<<48) + 
                            (((long)buf[off+5] & 0xff)<<40) + 
                            (((long)buf[off+4] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
        }
    }
    static final LongSupplier getLongUnsignedLittleEndian(int off, int len, byte[] buf)
    {
        switch (len)
        {
            case 1:
                return ()->buf[off] & 0xff;
            case 2:
                return ()-> (((buf[off+1] & 0xff)<<8)) + 
                            (buf[off] & 0xff);
            case 3:
                return ()-> ((buf[off+2] & 0xff)<<16) + 
                            ((buf[off+1] & 0xff)<<8) + 
                            (buf[off] & 0xff);
            case 4:
                return ()-> (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 5:
                return ()-> (((long)buf[off+4] & 0xff)<<32) + 
                            ((long)(buf[off+3] & 0xff)<<24) + 
                            ((long)(buf[off+2] & 0xff)<<16) + 
                            ((long)(buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 6:
                return ()-> (((long)buf[off+5] & 0xff)<<40) + 
                            (((long)buf[off+4] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            case 7:
                return ()-> (((long)buf[off+6] & 0xff)<<48) + 
                            (((long)buf[off+5] & 0xff)<<40) + 
                            (((long)buf[off+4] & 0xff)<<32) + 
                            (((long)buf[off+3] & 0xff)<<24) + 
                            (((long)buf[off+2] & 0xff)<<16) + 
                            (((long)buf[off+1] & 0xff)<<8) + 
                            ((long)buf[off] & 0xff);
            default:
                throw new UnsupportedOperationException(len+" not supported");
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
