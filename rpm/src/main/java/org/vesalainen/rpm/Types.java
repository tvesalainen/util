/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.rpm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tkv
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
    public static short[] getInt16(ByteBuffer bb, int index, int count)
    {
        short[] buf = new short[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.getShort(index+2*ii);
        }
        return buf;
    }
    public static int[] getInt32(ByteBuffer bb, int index, int count)
    {
        int[] buf = new int[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.getInt(index+4*ii);
        }
        return buf;
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
}
