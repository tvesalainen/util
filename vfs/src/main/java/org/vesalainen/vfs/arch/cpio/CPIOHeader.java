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
package org.vesalainen.vfs.arch.cpio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.util.CharSequences;
import org.vesalainen.vfs.arch.Header;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.unix.UnixFileAttributeView;
import org.vesalainen.vfs.unix.UnixFileAttributeViewImpl;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CPIOHeader extends Header
{
    static final String TRAILER = "TRAILER!!!";
    private static final int HEADER_SIZE = 110;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(HEADER_SIZE);
    private UnixFileAttributeView view;
    private static final byte[] MAGIC = "070701".getBytes(US_ASCII);
    private byte[] buf = new byte[8];
    private CharSequence seq = CharSequences.getAsciiCharSequence(buf);
    private byte[] magic = MAGIC;
    private int inode;
    private int mode;
    private int uid;
    private int gid;
    private int nlink = 1;
    private int mtime;
    private int filesize;
    private int devmajor;
    private int devminor;
    private int rdevmajor;
    private int rdevminor;
    private int namesize;
    private int checksum;
    private String filename;

    public CPIOHeader()
    {
        view = new UnixFileAttributeViewImpl(this);
    }
    
    @Override
    public boolean isEof()
    {
        return TRAILER.equals(filename);
    }

    @Override
    public String filename()
    {
        return filename;
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        align(channel, 4);
        buffer.clear();
        int rc = channel.read(buffer);
        buffer.flip();
        if (rc != HEADER_SIZE)
        {
            throw new IOException(rc+" input too small");
        }
        buffer.get(magic);
        if (!Arrays.equals(MAGIC, magic))
        {
            throw new IllegalArgumentException("not a CPIO");
        }
        inode = get(buffer);
        put(INODE, inode);
        mode = get(buffer);
        if (PosixHelp.isRegularFile((short) mode))
        {
            put(IS_REGULAR, true);
        }
        if (PosixHelp.isDirectory((short) mode))
        {
            put(IS_DIRECTORY, true);
        }
        if (PosixHelp.isSymbolicLink((short) mode))
        {
            put(IS_SYMBOLIC_LINK, true);
        }
        view.mode(mode);
        uid = get(buffer);
        gid = get(buffer);
        nlink = get(buffer);
        mtime = get(buffer);
        view.setTimes(FileTime.from(mtime, TimeUnit.SECONDS), null, null);
        filesize = get(buffer);
        attributes.put(FileAttributeName.getInstance(SIZE), (long)filesize);
        devmajor = get(buffer);
        devminor = get(buffer);
        rdevmajor = get(buffer);
        rdevminor = get(buffer);
        namesize = get(buffer);
        checksum = get(buffer);
        buffer.clear();
        buffer.limit(namesize);
        rc = channel.read(buffer);
        if (rc != namesize)
        {
            throw new IOException(rc+" imput too small");
        }
        byte[] b = new byte[namesize];
        buffer.flip();
        buffer.get(b);
        filename = new String(b, US_ASCII);
        skip(channel, 1);
        align(channel, 4);
    }

    @Override
    public void store(SeekableByteChannel channel, Map<String, Object> attributes) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storeEof(SeekableByteChannel channel) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void put(ByteBuffer bb, int v) throws IOException
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
    private int get(ByteBuffer bb) throws IOException
    {
        bb.get(buf);
        return Primitives.parseInt(seq, 16);
    }
}
