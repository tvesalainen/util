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
import org.vesalainen.lang.Casts;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.util.HexDump;
import org.vesalainen.vfs.arch.FileFormat;
import static org.vesalainen.vfs.arch.Header.Type.*;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.unix.Inode;
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
    private Map<Inode,String> inodes = new HashMap<>();
    private Map<FileFormat,Handler> handlers = new HashMap<>(); 
    private Handler handler;

    public CPIOHeader()
    {
        handlers.put(FileFormat.CPIO_NEWC, new NewAsciiHeader());
        handlers.put(FileFormat.CPIO_CRC, new CRCHeader());
        handlers.put(FileFormat.CPIO_BIN, new BinaryHeader());
        handlers.put(FileFormat.CPIO_ODC, new AsciiHeader());
    }

    @Override
    public boolean isEof()
    {
        return TRAILER.equals(filename);
    }

    @Override
    public void clear()
    {
        super.clear();
        namesize = 0;
        checksum = 0;
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        buffer.clear();
        if (handler == null)
        {
            buffer.limit(6);
            channel.read(buffer);
            for (Handler h : handlers.values())
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
            Inode in = new Inode(devminor, inode);
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
            toAttributes();
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
    public void store(SeekableByteChannel channel, String filename, FileFormat format, String linkname, Map<String, Object> attributes, byte[] digest) throws IOException
    {
        addAll(attributes);
        this.filename = filename;
        if (!isEof())
        {
            fromAttributes();
        }
        namesize = filename.length()+1;
        handler = handlers.get(format);
        int chksum = 0;
        if (digest != null)
        {
            chksum = Primitives.readInt(digest);
        }
        handler.store(channel, chksum);
        /*
        switch (format)
        {
            case CPIO_NEWC:
                putNewC(channel, (byte)'1', 0);
                break;
            case CPIO_CRC:
                putNewC(channel, (byte)'2', Primitives.readInt(digest));
                break;
            case CPIO_ODC:
                putAscii(channel, (byte)'7');
                break;
        }
        */
    }
    private void putNewC(SeekableByteChannel channel, byte id, int chksum) throws IOException
    {
        ChannelHelper.align(channel, 4);
        buffer.clear();
        buffer.put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7').put((byte)'0').put(id);
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
        put(buffer, filename.length()+1, 8, 16);
        put(buffer, checksum, 8, 16);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
        buffer.put(filename.getBytes(US_ASCII));
        buffer.put((byte)0);
        buffer.flip();
        channel.write(buffer);
        ChannelHelper.align(channel, 4);
    }
    @Override
    public void storeEof(SeekableByteChannel channel, FileFormat format) throws IOException
    {
        filename = TRAILER;
        handler = handlers.get(format);
        handler.store(channel, 0);
    }
    
    @Override
    public boolean supportsDigest()
    {
        return digestAlgorithm() != null && checksum != 0;
    }
    
    @Override
    public byte[] digest()
    {
        return Primitives.writeInt(checksum);
    }

    @Override
    public String digestAlgorithm()
    {
        if (handler != null)
        {
            return handler.digestAlgorithm();
        }
        else
        {
            return CPIO_CHECKSUM;
        }
    }

    @Override
    public long size()
    {
        switch (type)
        {
            case REGULAR:
            case SYMBOLIC:
                return size;
            default:
                return 0;
        }
    }
    
    private void put(ByteBuffer bb, short v, int length, int radix) throws IOException
    {
        put(bb, Casts.castUnsignedLong(v), length, radix);
    }
    private void put(ByteBuffer bb, int v, int length, int radix) throws IOException
    {
        put(bb, Casts.castUnsignedLong(v), length, radix);
    }
    private void put(ByteBuffer bb, long v, int length, int radix) throws IOException
    {
        Primitives.toDigits(v, length, radix).forEach((i)->bb.put((byte)i));
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
        protected abstract void store(SeekableByteChannel ch, int chksum) throws IOException;
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
        protected void store(SeekableByteChannel ch, int chksum) throws IOException
        {
            store(ch, (byte)'2', chksum);
        }

        @Override
        protected String digestAlgorithm()
        {
            return CPIO_CHECKSUM;
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
            ChannelHelper.align(ch, 4);
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
            mode = Casts.castUnsignedShort(getInt(bb, 8, 16));
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
            ChannelHelper.skip(channel, 1);
        }

        @Override
        protected void store(SeekableByteChannel ch, int chksum) throws IOException
        {
            store(ch, (byte)'1', chksum);
        }

        protected void store(SeekableByteChannel channel, byte id, int chksum) throws IOException
        {
            align(channel);
            buffer.clear();
            buffer.put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7').put((byte)'0').put(id);
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
            put(buffer, filename.length()+1, 8, 16);
            put(buffer, checksum, 8, 16);
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
            buffer.put(filename.getBytes(US_ASCII));
            buffer.put((byte)0);
            buffer.flip();
            channel.write(buffer);
            align(channel);
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
            mode = Casts.castUnsignedShort(getInt(bb, 6, 8));
            uid = getInt(bb, 6, 8);
            gid = getInt(bb, 6, 8);
            nlink = getInt(bb, 6, 8);
            rdevminor = getInt(bb, 6, 8);
            mtime = getInt(bb, 11, 8);
            namesize = getInt(bb, 6, 8);
            size = getInt(bb, 11, 8);
            filename = readString(channel, bb, namesize-1);
            ChannelHelper.skip(channel, 1);
        }

        @Override
        protected void store(SeekableByteChannel channel, int chksum) throws IOException
        {
        buffer.clear();
        buffer.put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7').put((byte)'0').put((byte)'7');
        put(buffer, rdevminor, 6, 8);
        put(buffer, inode, 6, 8);
        put(buffer, mode, 6, 8);
        put(buffer, uid, 6, 8);
        put(buffer, gid, 6, 8);
        put(buffer, nlink, 6, 8);
        put(buffer, 0, 6, 8);
        put(buffer, mtime, 11, 8);
        put(buffer, filename.length()+1, 6, 8);
        put(buffer, size, 11, 8);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
        buffer.put(filename.getBytes(US_ASCII));
        buffer.put((byte)0);
        buffer.flip();
        channel.write(buffer);
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
            ChannelHelper.align(ch, 2);
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
            ChannelHelper.skip(channel, 1);
            align(channel);
        }

        @Override
        protected void store(SeekableByteChannel channel, int chksum) throws IOException
        {
            align(channel);
            buffer.clear();
            buffer.putShort((short)070707);
            buffer.putShort((short)devminor);
            buffer.putShort((short)inode);
            buffer.putShort((short)mode);
            buffer.putShort((short)uid);
            buffer.putShort((short)gid);
            buffer.putShort((short)nlink);
            buffer.putShort((short)0);
            buffer.putShort((short) (mtime>>16));
            buffer.putShort((short) (mtime & 0xffff));
            buffer.putShort((short) (filename.length()+1));
            buffer.putShort((short) (size>>16));
            buffer.putShort((short) (size & 0xffff));
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
            buffer.put(filename.getBytes(US_ASCII));
            buffer.put((byte)0);
            buffer.flip();
            channel.write(buffer);
            align(channel);
        }

    }
}
