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
package org.vesalainen.pm.rpm;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.FilterByteBuffer;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author tkv
 */
public class CPIO
{
    private static final byte[] MAGIC = "070701".getBytes(US_ASCII);
    private byte[] buf = new byte[8];
    private CharSequence seq = CharSequences.getAsciiCharSequence(buf);
    byte[] magic = MAGIC;
    int ino;
    int mode;
    int uid;
    int gid;
    int nlink = 1;
    int mtime;
    int filesize;
    int devmajor;
    int devminor;
    int rdevmajor;
    int rdevminor;
    int namesize;
    int checksum;

    public CPIO()
    {
    }

    public CPIO(FilterByteBuffer bb) throws IOException
    {
        bb.get(magic);
        if (!Arrays.equals(MAGIC, magic))
        {
            throw new IllegalArgumentException("not a CPIO");
        }
        ino = get(bb);
        mode = get(bb);
        uid = get(bb);
        gid = get(bb);
        nlink = get(bb);
        mtime = get(bb);
        filesize = get(bb);
        devmajor = get(bb);
        devminor = get(bb);
        rdevmajor = get(bb);
        rdevminor = get(bb);
        namesize = get(bb);
        checksum = get(bb);
    }
    public void save(FilterByteBuffer bb) throws IOException
    {
        bb.put(magic);
        put(bb, ino);
        put(bb, mode);
        put(bb, uid);
        put(bb, gid);
        put(bb, nlink);
        put(bb, mtime);
        put(bb, filesize);
        put(bb, devmajor);
        put(bb, devminor);
        put(bb, rdevmajor);
        put(bb, rdevminor);
        put(bb, namesize);
        put(bb, checksum);
    }
    private void put(FilterByteBuffer bb, int v) throws IOException
    {
        for (int ii=7;ii>=0;ii--)
        {
            int i = v & 0xf;
            if (i < 10)
            {
                buf[ii] = (byte) ('0'+i);
            }
            else
            {
                buf[ii] = (byte) ('a'+i-10);
            }
            v = v>>4;
        }
        bb.put(buf);
    }
    private int get(FilterByteBuffer bb) throws IOException
    {
        bb.get(buf);
        return Primitives.parseInt(seq, 16);
    }
}
