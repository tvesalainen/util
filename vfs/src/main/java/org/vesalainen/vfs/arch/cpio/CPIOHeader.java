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
import org.vesalainen.vfs.unix.UnixFileAttributes;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CPIOHeader extends UnixFileHeader
{
    static final String TRAILER = "TRAILER!!!";
    private static final int HEADER_SIZE = 110;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(HEADER_SIZE);
    private UnixFileAttributeView view;
    private UnixFileAttributes unix;
    private static final byte[] MAGIC = "070701".getBytes(US_ASCII);
    private byte[] buf = new byte[8];
    private CharSequence seq = CharSequences.getAsciiCharSequence(buf);
    private byte[] magic = MAGIC;
    private int namesize;
    private int checksum;

    public CPIOHeader()
    {
        view = new UnixFileAttributeViewImpl(this);
        unix = view.readAttributes();
    }
    
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
        mode = get(buffer);
        uid = get(buffer);
        gid = get(buffer);
        nlink = get(buffer);
        mtime = get(buffer);
        filesize = get(buffer);
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
        filename = new String(b, 0, b.length-1, US_ASCII);
        skip(channel, 1);
        if (!isEof())
        {
            // set attributes
            put(INODE, inode);
            if (PosixHelp.isRegularFile((short) mode))
            {
                put(IS_REGULAR, true);
            }
            else
            {
                if (PosixHelp.isDirectory((short) mode))
                {
                    put(IS_DIRECTORY, true);
                }
                else
                {
                    if (PosixHelp.isSymbolicLink((short) mode))
                    {
                        put(IS_SYMBOLIC_LINK, true);
                    }
                }
            }
            view.mode(mode);
            view.setTimes(FileTime.from(mtime, TimeUnit.SECONDS), null, null);
            attributes.put(FileAttributeName.getInstance(SIZE), (long)filesize);
        }        
        align(channel, 4);
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
            filesize = (int) unix.size();
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
        put(buffer, mtime);
        put(buffer, filesize);
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
