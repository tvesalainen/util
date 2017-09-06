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
import static java.nio.ByteOrder.*;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.util.HexDump;
import static org.vesalainen.vfs.arch.Header.Type.*;
import static org.vesalainen.vfs.arch.Header.align;
import static org.vesalainen.vfs.attributes.FileAttributeName.CRC32;
import static org.vesalainen.vfs.attributes.FileAttributeName.DIGEST_VIEW;
import org.vesalainen.vfs.unix.INode;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CPIOHeader extends UnixFileHeader
{
    static final String TRAILER = "TRAILER!!!";
    private ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    private int namesize;
    private int checksum;
    private Map<INode,String> inodes = new HashMap<>();
    private Handler[] handlers = new Handler[] 
    {
        new NewAsciiHeader(),
        new CRCHeader(),
        new BinaryHeader(),
        new AsciiHeader()
    };
    private Handler handler;

    @Override
    public boolean isEof()
    {
        return TRAILER.equals(filename);
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        buffer.clear();
        if (handler == null)
        {
            buffer.limit(6);
            channel.read(buffer);
            for (Handler h : handlers)
            {
                if (h.isMyHeader(buffer))
                {
                    handler = h;
                    break;
                }
            }
            if (handler == null)
            {
                throw new UnsupportedOperationException(HexDump.toHex(buffer, 0, 6)+" not supported magic");
            }
        }
        handler.load(channel, buffer);
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
            handler.align(channel);
            if (type == SYMBOLIC)
            {
                linkname = readString(channel, buffer, (int) size);
            }
            updateAttributes();
        }        
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
        buffer.put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'1');
        put(buffer, inode, 8, 16);
        put(buffer, mode, 8, 16);
        put(buffer, uid, 8, 16);
        put(buffer, gid, 8, 16);
        put(buffer, nlink, 8, 16);
        put(buffer, mtime, 8, 16);
        put(buffer, size, 8, 16);
        put(buffer, devmajor, 8, 16);
        put(buffer, devminor, 8, 16);
        put(buffer, rdevmajor, 8, 16);
        put(buffer, rdevminor, 8, 16);
        put(buffer, namesize, 8, 16);
        put(buffer, checksum, 8, 16);
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
        buffer.put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'1');
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 1, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, 0, 8, 16);
        put(buffer, TRAILER.length()+1, 8, 16);
        put(buffer, 0, 8, 16);
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

    @Override
    public byte[] digest()
    {
        return Primitives.writeLong(checksum);
    }

    @Override
    public String digestAlgorithm()
    {
        return handler.digestAlgorithm();
    }
    
    private void put(ByteBuffer bb, long v, int length, int radix) throws IOException
    {
        long div = radix;
        for (int ii=2;ii<length;ii++)
        {
            div *= radix;
        }
        for (int ii=0;ii<length;ii++)
        {
            bb.put((byte) Character.forDigit((int) (v/div), radix));
            div /= radix;
        }
    }
    private int getInt(ByteBuffer bb, int length, int radix) throws IOException
    {
        int res = 0;
        for (int ii=0;ii<length;ii++)
        {
            char cc = (char) bb.get();
            res = radix*res + Character.digit(cc, radix);
        }
        return res;
    }
    private long getLong(ByteBuffer bb, int length, int radix) throws IOException
    {
        long res = 0;
        for (int ii=0;ii<length;ii++)
        {
            char cc = (char) bb.get();
            res = radix*res + Character.digit(cc, radix);
        }
        return res;
    }
    private abstract class Handler
    {
        protected abstract void align(SeekableByteChannel ch) throws IOException;
        protected abstract boolean isMyHeader(ByteBuffer buffer);
        protected abstract void load(SeekableByteChannel ch, ByteBuffer buffer) throws IOException;
        protected abstract void store(SeekableByteChannel ch) throws IOException;
        protected String digestAlgorithm()
        {
            return null;
        }
    }
    private class CRCHeader extends NewAsciiHeader
    {
        @Override
        protected boolean isMyHeader(ByteBuffer buffer)
        {
            for (int ii=0;ii<6;ii++)
            {
                if (buffer.get(ii) != "070702".charAt(ii))
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected String digestAlgorithm()
        {
            return CRC32;
        }
        
    }
    private class NewAsciiHeader extends Handler
    {

        @Override
        protected boolean isMyHeader(ByteBuffer buffer)
        {
            for (int ii=0;ii<6;ii++)
            {
                if (buffer.get(ii) != "070701".charAt(ii))
                {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        protected void align(SeekableByteChannel ch) throws IOException
        {
            UnixFileHeader.align(ch, 4);
        }

        @Override
        protected void load(SeekableByteChannel channel, ByteBuffer bb) throws IOException
        {
            if (bb.position() == 0) // not the first time
            {
                align(channel);
            }
            bb.limit(110);
            int rc = channel.read(bb);
            bb.flip();
            if (bb.remaining() != 110)
            {
                throw new IOException(rc+" input too small");
            }
            bb.position(6);
            inode = getInt(bb, 8, 16);
            mode = getInt(bb, 8, 16);
            uid = getInt(bb, 8, 16);
            gid = getInt(bb, 8, 16);
            nlink = getInt(bb, 8, 16);
            mtime = getInt(bb, 8, 16);
            size = getInt(bb, 8, 16);
            devmajor = getInt(bb, 8, 16);
            devminor = getInt(bb, 8, 16);
            rdevmajor = getInt(bb, 8, 16);
            rdevminor = getInt(bb, 8, 16);
            namesize = getInt(bb, 8, 16);
            checksum = getInt(bb, 8, 16);
            filename = readString(channel, bb, namesize-1);
            skip(channel, 1);
        }

        @Override
        protected void store(SeekableByteChannel ch)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
    private class AsciiHeader extends Handler
    {

        @Override
        protected boolean isMyHeader(ByteBuffer buffer)
        {
            for (int ii=0;ii<6;ii++)
            {
                if (buffer.get(ii) != "070707".charAt(ii))
                {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        protected void align(SeekableByteChannel ch) throws IOException
        {
        }

        @Override
        protected void load(SeekableByteChannel channel, ByteBuffer bb) throws IOException
        {
            bb.limit(76);
            int rc = channel.read(bb);
            bb.flip();
            if (bb.remaining() != 76)
            {
                throw new IOException(rc+" input too small");
            }
            bb.position(6);
            devminor = getInt(bb, 6, 8);
            inode = getInt(bb, 6, 8);
            mode = getInt(bb, 6, 8);
            uid = getInt(bb, 6, 8);
            gid = getInt(bb, 6, 8);
            nlink = getInt(bb, 6, 8);
            rdevminor = getInt(bb, 6, 8);
            mtime = getInt(bb, 11, 8);
            namesize = getInt(bb, 6, 8);
            size = getInt(bb, 11, 8);
            filename = readString(channel, bb, namesize-1);
            skip(channel, 1);
        }

        @Override
        protected void store(SeekableByteChannel ch)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
    private class BinaryHeader extends Handler
    {

        @Override
        protected boolean isMyHeader(ByteBuffer buffer)
        {
            buffer.order(BIG_ENDIAN);
            short magic = buffer.getShort(0);
            if (magic == 070707)
            {
                return true;
            }
            buffer.order(LITTLE_ENDIAN);
            magic = buffer.getShort(0);
            if (magic == 070707)
            {
                return true;
            }
            return false;
        }
        
        @Override
        protected void align(SeekableByteChannel ch) throws IOException
        {
            UnixFileHeader.align(ch, 2);
        }

        @Override
        protected void load(SeekableByteChannel channel, ByteBuffer bb) throws IOException
        {
            if (bb.position() == 0) // not the first time
            {
                align(channel);
            }
            bb.limit(26);
            int rc = channel.read(bb);
            bb.flip();
            if (bb.remaining() != 26)
            {
                throw new IOException(rc+" input too small");
            }
            short magic = buffer.getShort();
            if (magic != 070707)
            {
                throw new IOException(magic+" magic invalid");
            }
            devminor = bb.getShort();
            inode = bb.getShort();
            mode = bb.getShort();
            uid = bb.getShort();
            gid = bb.getShort();
            nlink = bb.getShort();
            rdevminor = bb.getShort();
            mtime = bb.getShort()*0x10000+bb.getShort();
            namesize = bb.getShort();
            size = bb.getShort()*0x10000+bb.getShort();
            filename = readString(channel, bb, namesize-1);
            skip(channel, 1);
        }

        @Override
        protected void store(SeekableByteChannel ch)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
