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

import java.nio.ByteOrder;
import java.util.function.LongSupplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ArrayFuncs
{
    public static final LongSupplier getLongSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
    {
        checkBits(offset, length, buf);
        boolean aligned8 = (offset % 8) == 0 && (length % 8) == 0;
        if (aligned8)
        {
            return getAlignedSupplier(offset, length, bigEndian, signed, buf);
        }
        else
        {
            return getSupplier(offset, length, bigEndian, signed, buf);
        }
        
    }
    static final LongSupplier getAlignedSupplier(int offset, int length, boolean bigEndian, boolean signed, byte... buf)
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
    static LongSupplier getSupplier(int offset, int length, boolean bigEndian, boolean signed, byte[] buf)
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
            long res = 0;
            for (int ii=0;ii<length;ii++)
            {
                int jj = offset+length-8+(ii%8)-8*(ii/8);
                res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
            }
            return res;
        };
    }
    static final LongSupplier getLongSignedLittleEndianX(int offset, int length, byte... buf)
    {
        return ()->
        {
            int jj = offset+length-8;
            long res = (buf[jj/8]>>(7-jj%8) & 0x1) == 1 ? -1 : 0;
            for (int ii=1;ii<length;ii++)
            {
                jj = offset+length-8+(ii%8)-8*(ii/8);
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

    private static void checkBits(int offset, int length, byte[] buf)
    {
        if (
                offset < 0 ||
                length < 0 ||
                length > 64 ||
                (offset + length) / 8 >= buf.length
                )
        {
            throw new IllegalArgumentException();
        }
    }

}
