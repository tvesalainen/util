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
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.vesalainen.lang.Casts;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.channels.AppendableByteChannel;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://en.wikipedia.org/wiki/Ar_(Unix)">ar (Unix)</a>
 */
public class ArHeader
{
    public static final int AR_HEADER_SIZE = 60;
    private String filename;
    private FileTime lastModifiedTime;
    private int uid;
    private int gid;
    private short mode;
    private int size;

    public ArHeader(String filename, int size)
    {
        if (filename.length() > 16)
        {
            throw new IllegalArgumentException(filename+" is too long");
        }
        if (filename.chars().anyMatch((c)->c>127))
        {
            throw new IllegalArgumentException(filename+" contains characters not us-ascii");
        }
        this.filename = filename;
        this.size = size;
        this.lastModifiedTime = FileTime.from(Instant.now());
        this.mode = 010644;
    }

    public ArHeader(SeekableByteChannel ch) throws IOException
    {
        ChannelHelper.align(ch, 2);
        ByteBuffer bb = ByteBuffer.allocate(AR_HEADER_SIZE);
        ch.read(bb);
        bb.flip();
        CharSequence seq = CharSequences.getAsciiCharSequence(bb);
        filename = seq.subSequence(0, 16).toString().trim();
        long mtime = getLong(seq, 16, 28, 10);
        lastModifiedTime = FileTime.from(mtime, TimeUnit.SECONDS);
        uid = Casts.castUnsignedInt(getLong(seq, 28, 34, 10));
        gid = Casts.castUnsignedInt(getLong(seq, 34, 40, 10));
        mode = Casts.castUnsignedShort(getLong(seq, 40, 48, 8));
        size = Casts.castUnsignedInt(getLong(seq, 48, 58, 10));
        ChannelHelper.align(ch, 2);
    }

    public void save(SeekableByteChannel ch) throws IOException
    {
        ChannelHelper.align(ch, 2);
        AppendableByteChannel abc = new AppendableByteChannel(ch, AR_HEADER_SIZE, false);
        Formatter formatter = new Formatter(abc);
        formatter.format(Locale.US, 
                "%-16s%-12d%-6d%-6d%-8o%-10d%c%c", 
                filename,
                lastModifiedTime.to(TimeUnit.SECONDS),
                uid,
                gid,
                mode,
                size,
                0x60,
                0x0a);
        formatter.flush();
        ChannelHelper.align(ch, 2);
    }
    public String getFilename()
    {
        return filename;
    }

    public FileTime getLastModifiedTime()
    {
        return lastModifiedTime;
    }

    public int getUid()
    {
        return uid;
    }

    public int getGid()
    {
        return gid;
    }

    public short getMode()
    {
        return mode;
    }

    public int getSize()
    {
        return size;
    }
    
    private long getLong(CharSequence seq, int start, int end, int radix)
    {
        int idx = CharSequences.indexOf(seq, (c)->!Character.isDigit(c), start);
        if (idx != -1)
        {
            end = idx;
        }
        return Primitives.parseLong(seq, radix, start, end);
    }
}
