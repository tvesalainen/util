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

import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;
import static org.vesalainen.rpm.RPM.LEAD_MAGIC;

/**
 *
 * @author tkv
 */
public class Lead
{
    byte[] magic = LEAD_MAGIC;
    byte major = 3;
    byte minor;
    short type;
    short archnum;
    String name;
    short osnum = 1;
    short signatureType = 5;
    byte[] reserved = new byte[16];

    public Lead(ByteBuffer bb)
    {
        bb.get(magic);
        if (!Arrays.equals(LEAD_MAGIC, magic))
        {
            throw new IllegalArgumentException("not rpm file");
        }
        major = bb.get();
        minor = bb.get();
        type = bb.getShort();
        archnum = bb.getShort();
        name = readString(bb, 66);
        osnum = bb.getShort();
        signatureType = bb.getShort();
        bb.get(reserved);
    }

    public Lead(String name)
    {
        this.name = name;
    }
    
    void save(ByteBuffer bb) throws IOException
    {
        bb.put(magic);
        bb.put(major);
        bb.put(minor);
        bb.putShort(type);
        bb.putShort(archnum);
        writeString(bb, name, 66);
        bb.putShort(osnum);
        bb.putShort(signatureType);
        bb.put(reserved);
    }
    private String readString(ByteBuffer bb, int size)
    {
        byte[] buf = new byte[size];
        bb.get(buf);
        int len = buf.length;
        for (int ii=0;ii<len;ii++)
        {
            if (buf[ii] == 0)
            {
                return new String(buf, 0, ii, US_ASCII);
            }
        }
        throw new IllegalArgumentException("no null terminator for string "+bb);
    }
    private void writeString(ByteBuffer bb, String str, int size)
    {
        byte[] buf = str.getBytes(US_ASCII);
        if (buf.length >= size)
        {
            throw new IllegalArgumentException(str+" length > "+size);
        }
        bb.put(buf);
        int left = size - buf.length;
        for (int ii=0;ii<left;ii++)
        {
            bb.put((byte)0);
        }
    }
}
