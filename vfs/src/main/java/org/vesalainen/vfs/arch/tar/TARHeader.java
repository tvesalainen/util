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
package org.vesalainen.vfs.arch.tar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.attribute.FileTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntPredicate;
import org.vesalainen.lang.Casts;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.CharSequences;
import org.vesalainen.vfs.arch.FileFormat;
import static org.vesalainen.vfs.arch.Header.Type.*;
import org.vesalainen.vfs.arch.cpio.SimpleChecksum;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/utilities/pax.html#tag_20_92_13_03">pax - portable archive interchange</a>
 */
public class TARHeader extends UnixFileHeader
{
    private static final byte[] GNU_MAGIC = new byte[]{'u', 's', 't', 'a', 'r', ' ', ' ', (byte)0};
    private static final byte[] USTAR_MAGIC = new byte[]{'u', 's', 't', 'a', 'r', (byte)0, '0', '0'};
    private static final int BLOCK_SIZE = 512;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private static final String USTAR = "ustar";
    private static final String GNU = "ustar  ";
    private static final String V7 = "";
    private String charset;
    private String hdrcharset;
    private boolean eof;
    private SimpleChecksum checksum = new SimpleChecksum();
    private String[] splitPath; // {path, prefix}

    @Override
    public void clear()
    {
        super.clear();
    }
    
    @Override
    public boolean isEof()
    {
        return eof;
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        align(channel, 512);
        buffer.clear();
        buffer.limit(BLOCK_SIZE);
        channel.read(buffer);
        buffer.flip();
        CharSequence seq = CharSequences.getAsciiCharSequence(buffer);
        eof = CharSequences.indexOf(seq, (c)->c!=0) == -1;
        if (eof)
        {
            buffer.clear();
            buffer.limit(BLOCK_SIZE);
            channel.read(buffer);
            return;
        }
        String magic = getString(seq, 257, 8);
        byte typeflag = buffer.get(156);
        int extHdrSize = getInt(seq, 124, 12);
        switch (typeflag)
        {
            case 'g':   // global
                switch (magic)
                {
                    case USTAR:
                        buffer.clear();
                        buffer.limit(BLOCK_SIZE);
                        channel.read(buffer);
                        buffer.flip();
                        readPaxheader(buffer);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported with global extension");
                }
                break;
        }
        switch (typeflag)
        {
            case 0:
            case '7':   
            case '0':   // regular
            case '1':   // hard linkname
            case '2':   // symbolic linkname
            case '5':   // directory
                switch (magic)
                {
                    case USTAR:
                    case GNU:
                    case V7:
                        readUstarHeader(buffer);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported");
                }
                break;
            case 'x':   // extended
                switch (magic)
                {
                    case USTAR:
                        ByteBuffer extendedHeader = getExtendedHeader(channel, extHdrSize);
                        readPaxheader(extendedHeader);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported with extension");
                }
                break;
            case 'g':   // global
                break;  // handled already
            case 'L':   
                switch (magic)
                {
                    case GNU:
                        ByteBuffer extendedHeader = getExtendedHeader(channel, extHdrSize);
                        readGnuLHeader(extendedHeader);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported with "+(char)typeflag+" typeflag");
                }
                break;  // handled already
            case 'K':   
                switch (magic)
                {
                    case GNU:
                        ByteBuffer extendedHeader = getExtendedHeader(channel, extHdrSize);
                        readGnuKHeader(extendedHeader);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported with "+(char)typeflag+" typeflag");
                }
                break;  // handled already
            default:
                throw new UnsupportedOperationException((char)typeflag+" not supported");
        }
        toAttributes();
    }
    private ByteBuffer getExtendedHeader(SeekableByteChannel channel, int extHdrSize) throws IOException
    {
        long extHdrBlockSize = nextBlock(extHdrSize);
        buffer.clear();
        buffer.limit(BLOCK_SIZE);
        channel.read(buffer);
        buffer.flip();
        ByteBuffer extendedHeader = buffer.slice();
        extendedHeader.limit(extHdrSize);
        buffer.position(BLOCK_SIZE);
        buffer.limit(BLOCK_SIZE + (int) extHdrBlockSize);
        channel.read(buffer);
        buffer.position(BLOCK_SIZE);
        readUstarHeader(buffer.slice());
        return extendedHeader;
    }
    private void readUstarHeader(ByteBuffer bb) throws IOException
    {
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        filename = getString(seq, 0, 100);
        mode = Casts.castUnsignedShort(getInt(seq, 100, 8));
        uid = getInt(seq, 108, 8);
        gid = getInt(seq, 116, 8);
        size = getLong(seq, 124, 12);
        mtime = getLong(seq, 136, 12);
        int chksum = getInt(seq, 148, 8);
        byte typeflag = bb.get(156);
        linkname = getString(seq, 157, 100);
        uname = getString(seq, 265, 32);
        gname = getString(seq, 297, 32);
        devmajor = getInt(seq, 329, 8);
        devminor = getInt(seq, 337, 8);
        String prefix = getString(seq, 345, 155);
        if (!prefix.isEmpty())
        {
            filename = prefix+'/'+filename;
        }
        // checksum
        for (int ii=0;ii<8;ii++)
        {
            bb.put(ii+148, (byte)' ');
        }
        checksum.reset();
        checksum.update(buffer);
        long value = checksum.getValue();
        if (chksum != value)
        {
            throw new IllegalArgumentException("checksum failed");
        }
        switch (typeflag)
        {
            case 0:
            case '7':   
            case '0':   // regular
                type = Type.REGULAR;
                break;
            case '2':   // symbolic linkname
                type = Type.SYMBOLIC;
                break;
            case '5':   // directory
                type = Type.DIRECTORY;
                break;
            case '1':   // hard linkname
                type = Type.HARD;
                break;
            default:
                throw new UnsupportedOperationException((char)typeflag+" not supported");
        }
    }
    private void readPaxheader(ByteBuffer bb)
    {
        while (bb.hasRemaining())
        {
            int length = readDecimal(bb);
            String key = readValue(bb, '=');
            switch (key)
            {
                case "atime":
                    atime = readLong(bb);
                    break;
                case "mtime":
                    mtime = readLong(bb);
                    break;
                case "ctime":
                    ctime = readLong(bb);
                    break;
                case "comment":
                    readValue(bb, '=');
                    break;
                case "uid":
                    uid = readDecimal(bb);
                    break;
                case "gid":
                    gid = readDecimal(bb);
                    break;
                case "uname":
                    uname = readValue(bb, '\n');
                    break;
                case "gname":
                    gname = readValue(bb, '\n');
                    break;
                case "linkpath":
                    linkname = readValue(bb, '\n');
                    break;
                case "path":
                    filename = readValue(bb, '\n');
                    break;
                case "size":
                    size = readLong(bb);
                    break;
                case "charset":
                    charset = readValue(bb, '\n');
                    break;
                case "hdrcharset":
                    hdrcharset = readValue(bb, '\n');
                    break;
                default:
                    throw new UnsupportedOperationException(key+" not supported");
            }
        }
    }
    private void readGnuLHeader(ByteBuffer bb) throws IOException
    {
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        filename = seq.subSequence(0, seq.length()-1).toString();
    }
    private void readGnuKHeader(ByteBuffer bb) throws IOException
    {
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        linkname = seq.subSequence(0, seq.length()-1).toString();
    }
    private long nextBlock(long position)
    {
        return position % BLOCK_SIZE > 0 ? (position / BLOCK_SIZE)*BLOCK_SIZE + BLOCK_SIZE : position;
    }
    private long readLong(ByteBuffer bb)
    {
        String string = readValue(bb, '\n');
        return (long) Double.parseDouble(string);
    }
    private int readDecimal(ByteBuffer bb)
    {
        int res = 0;
        char cc = (char) bb.get();
        while (Character.isDigit(cc))
        {
            res = 10*res+Character.digit(cc, 10);
            cc = (char) bb.get();
        }
        return res;
    }
    private String readValue(ByteBuffer bb, char end)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = bb.get();
        while (b != end)
        {
            baos.write(b);
            b = bb.get();
        }
        return new String(baos.toByteArray(), UTF_8);
    }
    @Override
    public void store(SeekableByteChannel channel, String filename, FileFormat format, String linkname, Map<String, Object> attributes, byte[] digest) throws IOException
    {
        align(channel, BLOCK_SIZE);
        addAll(attributes);
        fromAttributes();
        mode &= 07777;
        this.filename = filename;
        splitPath = splitFilename(filename);
        this.linkname = linkname;
        if (type == REGULAR && linkname != null)
        {
            type = HARD;
            size = 0;
        }
        if (type == SYMBOLIC)
        {
            size = 0;
        }
        switch (format)
        {
            case TAR_PAX:
                writePaxHeader(channel);
                break;
            case TAR_GNU:
                writeGnuHeader(channel);
                break;
            case TAR_USTAR:
                writeUstarHeader(channel);
                break;
            default:
                throw new UnsupportedOperationException(format+" not supported");
        }
    }

    private void writePaxHeader(SeekableByteChannel channel) throws IOException
    {
        buffer.position(BLOCK_SIZE).limit(buffer.capacity());
        putPaxHeader(buffer);
        int extLen = (int) buffer.position()-BLOCK_SIZE;
        int extHdrLen = (int) nextBlock(extLen);
        buffer.position(0).limit(BLOCK_SIZE);
        putUstarHeader(buffer, "./PaxHeaders.5208/"+filename, extLen, (byte)'x', linkname, USTAR_MAGIC, null);
        buffer.limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE).position(BLOCK_SIZE+extHdrLen);
        if (splitPath != null)
        {
            putUstarHeader(buffer, splitPath[0], size, typeFlagFromAttributes(), linkname, USTAR_MAGIC, splitPath[1]);
        }
        else
        {
            putUstarHeader(buffer, filename, size, typeFlagFromAttributes(), linkname, USTAR_MAGIC, null);
        }
        buffer.position(0).limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE);
        channel.write(buffer);
    }

    private void writeGnuHeader(SeekableByteChannel channel) throws IOException
    {
        if (filename.chars().anyMatch((c)->c>127))
        {
            throw new UnsupportedOperationException(filename+" characters not us-ascii not supported");
        }
        if (linkname != null && linkname.chars().anyMatch((c)->c>127))
        {
            throw new UnsupportedOperationException(linkname+" characters not us-ascii not supported");
        }
        if (splitPath != null && (linkname == null || linkname.length() <= 100))
        {
            buffer.position(0).limit(BLOCK_SIZE);
            putUstarHeader(buffer, splitPath[0], size, typeFlagFromAttributes(), linkname, GNU_MAGIC, splitPath[1]);
            buffer.position(0).limit(BLOCK_SIZE);
            channel.write(buffer);
        }
        else
        {
            if (splitPath == null)
            {
                buffer.position(BLOCK_SIZE).limit(buffer.capacity());
                buffer.put(filename.getBytes(US_ASCII)).put((byte)0);
                int extLen = (int) buffer.position()-BLOCK_SIZE;
                int extHdrLen = (int) nextBlock(extLen);
                buffer.position(0).limit(BLOCK_SIZE);
                putUstarHeader(buffer, "././@LongLink/"+filename, extLen, (byte)'L', linkname, GNU_MAGIC, null);
                buffer.limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE).position(BLOCK_SIZE+extHdrLen);
                putUstarHeader(buffer, filename, size, typeFlagFromAttributes(), linkname, GNU_MAGIC, null);
                buffer.position(0).limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE);
                channel.write(buffer);
            }
            if (linkname != null && linkname.length() > 100)
            {
                buffer.position(BLOCK_SIZE).limit(buffer.capacity());
                buffer.put(linkname.getBytes(US_ASCII)).put((byte)0);
                int extLen = (int) buffer.position()-BLOCK_SIZE;
                int extHdrLen = (int) nextBlock(extLen);
                buffer.position(0).limit(BLOCK_SIZE);
                putUstarHeader(buffer, "././@LongLink/"+filename, extLen, (byte)'K', linkname, GNU_MAGIC, null);
                buffer.limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE).position(BLOCK_SIZE+extHdrLen);
                putUstarHeader(buffer, filename, size, typeFlagFromAttributes(), linkname, GNU_MAGIC, null);
                buffer.position(0).limit(BLOCK_SIZE+extHdrLen+BLOCK_SIZE);
                channel.write(buffer);
            }
        }
    }
    private void writeUstarHeader(SeekableByteChannel channel) throws IOException
    {
        if (filename.chars().anyMatch((c)->c>127))
        {
            throw new UnsupportedOperationException(filename+" characters not us-ascii not supported");
        }
        if (linkname != null && linkname.chars().anyMatch((c)->c>127))
        {
            throw new UnsupportedOperationException(linkname+" characters not us-ascii not supported");
        }
        if (splitPath == null)
        {
            throw new UnsupportedOperationException(filename+" too long");
        }
        if (linkname != null && linkname.length() > 100)
        {
            throw new UnsupportedOperationException(linkname+" too long");
        }
        buffer.position(0).limit(BLOCK_SIZE);
        putUstarHeader(buffer, splitPath[0], size, typeFlagFromAttributes(), linkname, USTAR_MAGIC, splitPath[1]);
        buffer.position(0).limit(BLOCK_SIZE);
        channel.write(buffer);
    }

    private byte typeFlagFromAttributes()
    {
        switch (type)
        {
            case REGULAR:
                return '0';
            case DIRECTORY:
                return '5';
            case SYMBOLIC:
                return '2';
            case HARD:
                return '1';
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    private void putPaxHeader(ByteBuffer bb) throws IOException
    {
        putPaxData(bb, "atime", getTime(LAST_ACCESS_TIME));
        putPaxData(bb, "ctime", getTime(CREATION_TIME));
        putPaxData(bb, "mtime", getTime(LAST_MODIFIED_TIME));
        if (gid > 07777777)
        {
            putPaxData(bb, "gid", gid);
        }
        if (uid > 07777777)
        {
            putPaxData(bb, "uid", uid);
        }
        if (linkname != null)
        {
            if (linkname.length() >= 100 || linkname.chars().anyMatch((c)->c>127))
            {
                putPaxData(bb, "linkpath", linkname);
            }
        }
        if (splitPath == null || filename.chars().anyMatch((c)->c>127))
        {
            putPaxData(bb, "path", filename);
        }
        if (size > 077777777777L)
        {
            putPaxData(bb, "size", size);
        }
    }
    private double getTime(String name)
    {
        FileTime time = (FileTime) get(name);
        double nano = time.to(TimeUnit.NANOSECONDS);
        return nano / 1e9;
    }
    private String[] splitFilename(String name)
    {
        int length = name.length();
        if (length <= 100)
        {
            return new String[] {name, null};
        }
        int idx = name.indexOf('/');
        while (idx != -1 && idx <= 135)
        {
            if (length - idx - 1 <= 100)
            {
                return new String[] {name.substring(idx+1), name.substring(0, idx)};
            }
            idx = name.indexOf('/', idx+1);
        }
        return null;    // don't fit
    }
    private void putPaxData(ByteBuffer bb, String key, double value) throws IOException
    {
        putPaxData(bb, key, String.format(Locale.US, "%f", value));
    }
    private void putPaxData(ByteBuffer bb, String key, long value) throws IOException
    {
        putPaxData(bb, key, Long.toString(value));
    }
    private void putPaxData(ByteBuffer bb, String key, String value) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(' ');
        baos.write(key.getBytes(UTF_8));
        baos.write('=');
        baos.write(value.getBytes(UTF_8));
        baos.write('\n');
        byte[] array = baos.toByteArray();
        int len = array.length+1;
        int ln = (int) Math.log10(len)+1+array.length;
        while (ln != len)
        {
            len = ln;
            ln = (int) Math.log10(len)+1+array.length;
        }
        byte[] lb = String.valueOf(len).getBytes(US_ASCII);
        bb.put(lb);
        bb.put(array);
    }
    private void putUstarHeader(ByteBuffer bb, String name, long siz, byte typeflag, String linkname, byte[] magic, String prefix)
    {
        ByteBuffers.clearRemaining(bb);
        ByteBuffer chkSumBuffer = bb.slice();
        put(bb, name, 100);
        put(bb, mode, 8);
        put(bb, uid, 8);
        put(bb, gid, 8);
        put(bb, siz, 12);
        put(bb, mtime, 12);
        int chksumMark = bb.position();
        put(bb, "        ", 8);
        bb.put(typeflag);
        put(bb, linkname, 100);
        assert magic.length == 8;   // magic + version
        bb.put(magic);
        put(bb, uname, 32);
        put(bb, gname, 32);
        put(bb, devmajor, 8);
        put(bb, devminor, 8);
        put(bb, prefix, 155);
        checksum.reset();
        checksum.update(chkSumBuffer);
        long value = checksum.getValue();
        bb.position(chksumMark);
        put(bb, value, 8);
    }
    private void put(ByteBuffer bb, long value, int length)
    {
        put(bb, value, length, 8);
    }
    private void put(ByteBuffer bb, long value, int length, int radix)
    {
        int len = length-1;
        long div = radix;
        for (int ii=2;ii<len;ii++)
        {
            div *= radix;
        }
        for (int ii=0;ii<len;ii++)
        {
            int v = (int) (Long.divideUnsigned(value, div)%radix);
            bb.put((byte) Character.forDigit(v , radix));
            div /= radix;
        }
        bb.put((byte)0);
    }
    private void put(ByteBuffer bb, String text, int length)
    {
        int newPosition = bb.position() + length;
        byte[] bytes;
        if (text != null)
        {
            bytes = text.getBytes(US_ASCII);
        }
        else
        {
            bytes = new byte[0];
        }
        bb.put(bytes, 0, Math.min(bytes.length, length));
        bb.position(newPosition);
    }
    @Override
    public void storeEof(SeekableByteChannel channel, FileFormat format) throws IOException
    {
        align(channel, BLOCK_SIZE);
        align(channel, BLOCK_SIZE);
        align(channel, BLOCK_SIZE);
    }

    @Override
    public byte[] digest()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String digestAlgorithm()
    {
        return null;
    }

    @Override
    public boolean supportsDigest()
    {
        return false;
    }

    @Override
    public long size()
    {
        return size;
    }
    
    private int getInt(CharSequence seq, int offset, int length)
    {
        CharSequence terminated = getTerminated(seq, offset, length, (c)->!Character.isDigit(c));
        if (terminated.length() > 0)
        {
            return Primitives.parseInt(terminated, 8);
        }
        else
        {
            return 0;
        }
    }
    private long getLong(CharSequence seq, int offset, int length)
    {
        CharSequence terminated = getTerminated(seq, offset, length, (c)->!Character.isDigit(c));
        if (terminated.length() > 0)
        {
            return Primitives.parseLong(terminated, 8);
        }
        else
        {
            return 0;
        }
    }
    private String getString(CharSequence seq, int offset, int length)
    {
        return getTerminated(seq, offset, length, (c)->c==0).toString();
    }
    private CharSequence getTerminated(CharSequence seq, int offset, int length, IntPredicate predicate)
    {
        int ii=0;
        for (;ii<length;ii++)
        {
            if (predicate.test(seq.charAt(ii + offset)))
            {
                break;
            }
        }
        return seq.subSequence(offset, ii+offset);
    }

}
