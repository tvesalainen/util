/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.pm.rpm;

import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.util.HexDump;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Types
{
    public static byte[] getChar(ByteBuffer bb, int index, int count)
    {
        byte[] buf = new byte[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.get(index+ii);
        }
        return buf;
    }
    public static byte[] getInt8(ByteBuffer bb, int index, int count)
    {
        return getChar(bb, index, count);
    }
    public static List<Short> getInt16(ByteBuffer bb, int index, int count)
    {
        List<Short> list = new ArrayList<>();
        for (int ii=0;ii<count;ii++)
        {
            list.add(bb.getShort(index+2*ii));
        }
        return list;
    }
    public static List<Integer> getInt32(ByteBuffer bb, int index, int count)
    {
        List<Integer> list = new ArrayList<>();
        for (int ii=0;ii<count;ii++)
        {
            list.add(bb.getInt(index+4*ii));
        }
        return list;
    }
    public static String getString(ByteBuffer bb, int index)
    {
        return getStringArray(bb, index, 1).get(0);
    }
    public static List<String> getStringArray(ByteBuffer bb, int index, int count)
    {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (int ii=index;count>0;ii++)
        {
            byte cc = bb.get(ii);
            if (cc == 0)
            {
                list.add(sb.toString());
                sb.setLength(0);
                count--;
            }
            else
            {
                sb.append((char)cc);
            }
        }
        return list;
    }
    public static byte[] getBin(ByteBuffer bb, int index, int count)
    {
        byte[] buf = new byte[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.get(index+ii);
        }
        return buf;
    }
    public static Object getData(ByteBuffer bb, int index, int count, IndexType type)
    {
        switch (type)
        {
            case INT16:
                return getInt16(bb, index, count);
            case INT32:
                return getInt32(bb, index, count);
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return getStringArray(bb, index, count);
            case BIN:
                return getBin(bb, index, count);
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    public static <T> int setData(ByteBuffer bb, List<T> list, byte[] bin, IndexType type)
    {
        switch (type)
        {
            case INT16:
                return setInt16(bb, (List<Short>) list);
            case INT32:
                return setInt32(bb, (List<Integer>) list);
            case STRING:
                if (list.size() != 1)
                {
                    throw new IllegalArgumentException("STRING size != 1 = "+list.size());
                }
            case I18NSTRING:
            case STRING_ARRAY:
                return setStringArray(bb, (List<String>) list);
            case BIN:
                return setBin(bb, bin);
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    private static int setInt16(ByteBuffer bb, List<Short> list)
    {
        RPM.align(bb, 2);
        int position = bb.position();
        for (Short s : list)
        {
            bb.putShort(s.shortValue());
        }
        return position;
    }

    private static int setInt32(ByteBuffer bb, List<Integer> list)
    {
        RPM.align(bb, 4);
        int position = bb.position();
        for (Integer i : list)
        {
            bb.putInt(i.intValue());
        }
        return position;
    }

    private static int setStringArray(ByteBuffer bb, List<String> list)
    {
        int position = bb.position();
        for (String s : list)
        {
            byte[] bytes = s.getBytes(US_ASCII);
            bb.put(bytes).put((byte)0);
        }
        return position;
    }

    private static int setBin(ByteBuffer bb, byte[] bin)
    {
        int position = bb.position();
        bb.put(bin);
        return position;
    }
    public static int getCount(Object data, IndexType type)
    {
        switch (type)
        {
            case INT16:
            case INT32:
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                List<?> list = (List<?>) data;
                return list.size();
            case BIN:
                byte[] buf = (byte[]) data;
                return buf.length;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

}
