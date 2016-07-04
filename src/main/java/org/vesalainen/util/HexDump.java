/*
 * Copyright (C) 2016 tkv
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

/**
 *
 * @author tkv
 */
public class HexDump
{
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
        sb.append("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n");
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
                if (!Character.isISOControl(Byte.toUnsignedInt(buf[offset + ii])))
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
