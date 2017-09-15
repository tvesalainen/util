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
package org.vesalainen.vfs.pm.rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Lead
{
    static final byte[] LEAD_MAGIC = new byte[]{(byte) 0xed, (byte) 0xab, (byte) 0xee, (byte) 0xdb};
    private byte[] magic = LEAD_MAGIC;
    private byte major = 3;
    private byte minor;
    private short type;
    private short archnum;
    private String name;
    private short osnum = 1;
    private short signatureType = 5;
    private byte[] reserved = new byte[16];
    private ByteBuffer bb = ByteBuffer.allocateDirect(96).order(BIG_ENDIAN);

    public Lead(SeekableByteChannel ch) throws IOException
    {
        bb.clear();
        ch.read(bb);
        bb.flip();
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
    
    void save(SeekableByteChannel ch) throws IOException
    {
        bb.clear();
        bb.put(magic);
        bb.put(major);
        bb.put(minor);
        bb.putShort(type);
        bb.putShort(archnum);
        writeString(bb, name, 66);
        bb.putShort(osnum);
        bb.putShort(signatureType);
        bb.put(reserved);
        bb.flip();
        ch.write(bb);
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
