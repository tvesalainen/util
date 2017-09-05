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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.util.CharSequences;
import static org.vesalainen.vfs.arch.Header.Type.*;
import org.vesalainen.vfs.unix.INode;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CPIOHeader extends UnixFileHeader
{
    static final String TRAILER = "TRAILER!!!";
    private static final int HEADER_SIZE = 110;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    private static final byte[] MAGIC = "070701".getBytes(US_ASCII);
    private byte[] buf = new byte[8];
    private CharSequence seq = CharSequences.getAsciiCharSequence(buf);
    private byte[] magic = Arrays.copyOf(MAGIC, MAGIC.length);
    private int namesize;
    private int checksum;
    private Map<INode,String> inodes = new HashMap<>();

    @Override
    public boolean isEof()
    {
        return TRAILER.equals(filename);
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        align(channel, 4);
        buffer.clear();
        buffer.limit(HEADER_SIZE);
        int rc = channel.read(buffer);
        buffer.flip();
        if (rc != HEADER_SIZE)
        {
            throw new IOException(rc+" input too small");
        }
        buffer.get(magic);
        if (!Arrays.equals(MAGIC, magic))
        {
            throw new IllegalArgumentException("not a newc CPIO");
        }
        inode = get(buffer);
        mode = get(buffer);
        uid = get(buffer);
        gid = get(buffer);
        nlink = get(buffer);
        mtime = get(buffer);
        size = get(buffer);
        devmajor = get(buffer);
        devminor = get(buffer);
        rdevmajor = get(buffer);
        rdevminor = get(buffer);
        namesize = get(buffer);
        checksum = get(buffer);
        filename = readString(channel, buffer, namesize-1);
        skip(channel, 1);
        if (!isEof())
        {
            INode in = new INode(devminor, inode);
            linkname = inodes.get(in);
            if (linkname == null)
            {
                inodes.put(in, filename);
            }
            if (linkname != null)
            {
                type = HARD;
            }
            else
            {
                if (PosixHelp.isRegularFile((short) mode))
                {
                    type = REGULAR;
                }
                else
                {
                    if (PosixHelp.isDirectory((short) mode))
                    {
                        type = DIRECTORY;
                    }
                    else
                    {
                        if (PosixHelp.isSymbolicLink((short) mode))
                        {
                            type = SYMBOLIC;
                        }
                    }
                }
            }
        }        
        align(channel, 4);
        if (type == SYMBOLIC)
        {
            linkname = readString(channel, buffer, (int) size);
        }
        updateAttributes();
    }
    private String readString(SeekableByteChannel channel, ByteBuffer bb, int length) throws IOException
    {
        bb.clear();
        bb.limit(length);
        int rc = channel.read(bb);
        if (rc != length)
        {
            throw new IOException(rc+" input too small");
        }
        byte[] b = new byte[length];
        bb.flip();
        bb.get(b);
        return new String(b, 0, b.length, US_ASCII);
    }
    @Override
    public void store(SeekableByteChannel channel, String fn, Map<String, Object> attributes) throws IOException
    {
        addAll(attributes);
        this.filename = fn;
        if (!isEof())
        {
            inode = unix.inode();
            mode = unix.mode();
            nlink = unix.nlink();
            mtime = (int) unix.lastModifiedTime().to(TimeUnit.SECONDS);
            size = (int) unix.size();
            devminor = unix.device();
        }
        namesize = fn.length()+1;
        align(channel, 4);
        buffer.clear();
        buffer.put(MAGIC);
        put(buffer, inode);
        put(buffer, mode);
        put(buffer, uid);
        put(buffer, gid);
        put(buffer, nlink);
        put(buffer, (int)mtime);
        put(buffer, (int)size);
        put(buffer, devmajor);
        put(buffer, devminor);
        put(buffer, rdevmajor);
        put(buffer, rdevminor);
        put(buffer, namesize);
        put(buffer, checksum);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
        buffer.put(fn.getBytes(US_ASCII));
        buffer.put((byte)0);
        buffer.flip();
        channel.write(buffer);
        align(channel, 4);
    }

    @Override
    public void storeEof(SeekableByteChannel channel) throws IOException
    {
        align(channel, 4);
        buffer.clear();
        buffer.put(MAGIC);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 1);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, 0);
        put(buffer, TRAILER.length()+1);
        put(buffer, 0);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
        buffer.put(TRAILER.getBytes(US_ASCII));
        buffer.put((byte)0);
        buffer.flip();
        channel.write(buffer);
        long alignedPosition = alignedPosition(channel, 4);
        buffer.clear();
        int skip = (int) (alignedPosition - channel.position());
        for (int ii=0;ii<skip;ii++)
        {
            buffer.put((byte)0);
        }
        buffer.flip();
        channel.write(buffer);
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
