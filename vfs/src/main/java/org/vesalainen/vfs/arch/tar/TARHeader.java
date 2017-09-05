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
import java.util.Map;
import java.util.function.IntPredicate;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.CharSequences;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/utilities/pax.html#tag_20_92_13_03">pax - portable archive interchange</a>
 */
public class TARHeader extends UnixFileHeader
{
    private static final int BLOCK_SIZE = 512;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private static final String USTAR = "ustar";
    private static final String GNU = "ustar  ";
    private static final String V7 = "";
    private String charset;
    private String hdrcharset;
    private boolean eof;

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
        long position = channel.position();
        long skip = nextBlock(position) - position;
        if (skip > 0)
        {
            buffer.clear();
            buffer.limit((int) skip);
            channel.read(buffer);
        }
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
                        paxHeader(buffer);
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
                        ustarHeader(buffer);
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
                        paxHeader(extendedHeader);
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
                        gnuLHeader(extendedHeader);
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
                        gnuKHeader(extendedHeader);
                        break;
                    default:
                        throw new UnsupportedOperationException("'"+magic+"' magic not supported with "+(char)typeflag+" typeflag");
                }
                break;  // handled already
            default:
                throw new UnsupportedOperationException((char)typeflag+" not supported");
        }
        updateAttributes();
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
        ustarHeader(buffer.slice());
        return extendedHeader;
    }
    private void ustarHeader(ByteBuffer bb) throws IOException
    {
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        filename = getString(seq, 0, 100);
        mode = getInt(seq, 100, 8);
        uid = getInt(seq, 108, 8);
        gid = getInt(seq, 116, 8);
        size = getInt(seq, 124, 12);
        mtime = getInt(seq, 136, 12);
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
            filename = prefix+filename;
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
    private void paxHeader(ByteBuffer bb)
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
    private void gnuLHeader(ByteBuffer bb) throws IOException
    {
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        filename = seq.subSequence(0, seq.length()-1).toString();
    }
    private void gnuKHeader(ByteBuffer bb) throws IOException
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
    public void store(SeekableByteChannel channel, String filename, Map<String, Object> attributes) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storeEof(SeekableByteChannel channel) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
