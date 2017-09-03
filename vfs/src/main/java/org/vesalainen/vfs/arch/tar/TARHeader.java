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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Map;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.CharSequences;
import org.vesalainen.vfs.unix.UnixFileAttributeView;
import org.vesalainen.vfs.unix.UnixFileAttributes;
import org.vesalainen.vfs.unix.UnixFileHeader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARHeader extends UnixFileHeader
{
    private static final int HEADER_SIZE = 512;
    private ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
    private UnixFileAttributeView view;
    private UnixFileAttributes unix;
    private static final byte[] MAGIC = "ustar".getBytes(US_ASCII);
    private CharSequence seq = CharSequences.getAsciiCharSequence(buffer.array());

    @Override
    public boolean isEof()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFilename()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(SeekableByteChannel channel) throws IOException
    {
        buffer.clear();
        channel.read(buffer);
        filename = getString(0, 100);
        mode = getInt(100, 8);
        uid = getInt(108, 8);
        gid = getInt(116, 8);
        filesize = getInt(124, 12);
        mtime = getInt(136, 12);
        int chksum = getInt(148, 8);
        int typeflag = getInt(156, 1);
        String linkname = getString(157, 100);
        String magic = getString(257, 6);
        //int version = getInt(263, 2);
        String uname = getString(265, 32);
        String gname = getString(297, 32);
        //devmajor = getInt(329, 8);
        //devminor = getInt(337, 8);
        String prefix = getString(345, 155);
        if (!prefix.isEmpty())
        {
            filename = prefix+filename;
        }
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
    
    private int getInt(int offset, int length)
    {
        return Primitives.parseInt(getZeroTerminated(offset, length), 8);
    }
    private String getString(int offset, int length)
    {
        return getZeroTerminated(offset, length).toString();
    }
    private CharSequence getZeroTerminated(int offset, int length)
    {
        int ii=0;
        for (;ii<length;ii++)
        {
            if (seq.charAt(ii + offset) == 0)
            {
                break;
            }
        }
        return seq.subSequence(offset, ii+offset);
    }
}
