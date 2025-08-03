/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HexDump
{
    private static final String Title = "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f";
    /**
     * Recreates byte array from hex dump.
     * @param hexDump
     * @return 
     */
    public static final byte[] fromHex(String hexDump)
    {
        List<Integer> list = new ArrayList<>();
        String[] lines = hexDump.split("\n");
        if (lines.length < 2)
        {
            throw new IllegalArgumentException("not a hex dump");
        }
        if (!lines[0].contains(Title))
        {
            throw new IllegalArgumentException("not a hex dump");
        }
        for (int ll=1;ll<lines.length;ll++)
        {
            String line = lines[ll];
            int i1 = line.indexOf(':');
            if (i1 == -1)
            {
                throw new IllegalArgumentException("not a hex dump");
            }
            int i2 = line.indexOf("  ", i1+2);
            if (i2 == -1)
            {
                throw new IllegalArgumentException("not a hex dump");
            }
            String[] fields = line.substring(i1+2, i2).split(" ");
            for (int ii=0;ii<fields.length;ii++)
            {
                list.add(Integer.parseInt(fields[ii], 16));
            }
        }
        byte[] bytes = new byte[list.size()];
        for (int ii=0;ii<bytes.length;ii++)
        {
            bytes[ii] = list.get(ii).byteValue();
        }
        return bytes;
    }
    /**
     * Creates readable view to byte buffer remaining. Doesn't change byte buffer.
     * @param bb
     * @return 
     */
    public static final String remainingToHex(ByteBuffer bb)
    {
        byte[] buf = new byte[bb.remaining()];
        int safe = bb.position();
        bb.get(buf);
        bb.position(safe);
        return toHex(buf);
    }
    /**
     * Creates readable view to byte buffer remaining. Mark will be discarded.
     * @param bb
     * @return 
     */
    public static final String startToHex(ByteBuffer bb)
    {
        byte[] buf = new byte[bb.position()];
        int safePos = bb.position();
        int safeLim = bb.limit();
        bb.position(0);
        bb.get(buf);
        bb.position(safePos);
        bb.limit(safeLim);
        return toHex(buf);
    }
    public static final String toHex(ByteBuffer bb, int offset, int length)
    {
        byte[] buf = new byte[length];
        int safePos = bb.position();
        int safeLim = bb.limit();
        bb.position(offset);
        bb.limit(offset+length);
        bb.get(buf);
        bb.limit(safeLim);
        bb.position(safePos);
        return toHex(buf);
    }
    /**
     * Creates readable view to byte array content.
     * @param buf
     * @param offset
     * @param length
     * @return 
     */
    public static final String toHex(byte[] buf, int offset, int length)
    {
        return toHex(Arrays.copyOfRange(buf, offset, offset+length));
    }
    /**
     * Creates readable view to byte array content.
     * @param supplier
     * @return 
     */
    public static final String toHex(Supplier<byte[]> supplier)
    {
        return toHex(supplier.get());
    }
    /**
     * Creates readable view to byte array content.
     * @param buf
     * @return 
     */
    public static final String toHex(byte[] buf)
    {
        StringBuilder sb = new StringBuilder();
        int prec = prec(buf.length);
        for (int ii=0;ii<prec;ii++)
        {
            sb.append(' ');
        }
        sb.append("  ");
        String format = "%0"+prec+"x: ";
        sb.append(Title);
        sb.append('\n');
        for (int ii=0;ii<buf.length;ii+=16)
        {
            printLine(sb, format, buf, ii, Math.min(16, buf.length - ii));
        }
        return sb.toString();
    }
    private static int prec(int length)
    {
        int p = 0;
        while (length != 0)
        {
            p++;
            length >>= 4;
        }
        return p;
    }
    private static void printLine(StringBuilder sb, String fmt, byte[] buf, int offset, int length)
    {
        sb.append(String.format(fmt, offset));
        for (int ii=0;ii<16;ii++)
        {
            if (ii < length)
            {
                hexAppend(sb, buf[offset + ii]);
                sb.append(' ');
            }
            else
            {
                sb.append("   ");
            }
        }
        sb.append(" ");
        for (int ii=0;ii<16;ii++)
        {
            if (ii < length)
            {
                if (isPrintable(Byte.toUnsignedInt(buf[offset + ii])))
                {
                    sb.append((char)buf[offset + ii]);
                }
                else
                {
                    sb.append('.');
                }
                sb.append(' ');
            }
        }
        sb.append('\n');
    }
    private static boolean isPrintable(int cc)
    {
        return cc >= 32 && cc <= 126;
    }
    private static void hexAppend(StringBuilder sb, byte b)
    {
        String s = Integer.toHexString(Byte.toUnsignedInt(b));
        if (s.length() < 2)
        {
            sb.append('0');
        }
        sb.append(s);
    }
}
