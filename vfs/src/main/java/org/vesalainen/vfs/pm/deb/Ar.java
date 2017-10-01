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
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.vesalainen.lang.Casts;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.channels.AppendableByteChannel;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.HexDump;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://en.wikipedia.org/wiki/Ar_(Unix)">ar (Unix)</a>
 */
public class Ar implements AutoCloseable
{
    public static final int AR_HEADER_SIZE = 60;
    private static final byte[] AR_MAGIC = "!<arch>\n".getBytes(US_ASCII);
    private SeekableByteChannel channel;
    private ArHeader header;
    private long pos;
    private boolean read;

    public Ar(SeekableByteChannel channel, boolean read) throws IOException
    {
        this.channel = channel;
        this.read = read;
        if (read)
        {
            byte[] magic = ChannelHelper.read(channel, AR_MAGIC.length);
            if (!Arrays.equals(AR_MAGIC, magic))
            {
                throw new UnsupportedOperationException(HexDump.toHex(magic)+" not an ar file");
            }
        }
        else
        {
            ChannelHelper.write(channel, AR_MAGIC);
        }
        pos = channel.position();
    }

    public ArHeader getEntry() throws IOException
    {
        channel.position(pos);
        ArHeader hdr = new ArHeader(channel);
        pos += AR_HEADER_SIZE + hdr.getSize();
        return hdr;
    }
    public void addEntry(String filename) throws IOException
    {
        addEntry(filename, FileTime.from(Instant.now()), 0, 0, (short)0100644);
    }
    public void addEntry(String filename, FileTime lastModified, int owner, int group, short mode) throws IOException
    {
        if (read)
        {
            throw new IllegalStateException("addEntry while reading");
        }
        if (filename.length() > 16)
        {
            throw new IllegalArgumentException(filename+" is too long");
        }
        if (filename.chars().anyMatch((c)->c>127))
        {
            throw new IllegalArgumentException(filename+" contains characters not us-ascii");
        }
        close();
        pos = channel.position();
        if ((pos % 2) != 0)
        {
            ChannelHelper.write(channel, new byte[]{'\n'});
            pos++;
        }
        ChannelHelper.skip(channel, AR_HEADER_SIZE);
        header = new ArHeader(filename, lastModified, owner, group, mode);
    }
    @Override
    public void close() throws IOException
    {
        if (!read && header != null)
        {
            long position = channel.position();
            header.size = position - pos - AR_HEADER_SIZE;
            channel.position(pos);
            header.save(channel);
            channel.position(position);
            header = null;
        }
    }
    public static class ArHeader
    {
        private String filename;
        private FileTime lastModifiedTime;
        private int uid;
        private int gid;
        private short mode;
        private long size;

        private ArHeader(String filename, FileTime lastModifiedTime, int uid, int gid, short mode)
        {
            this.filename = filename;
            this.lastModifiedTime = lastModifiedTime;
            this.uid = uid;
            this.gid = gid;
            this.mode = mode;
        }

        private ArHeader(SeekableByteChannel ch) throws IOException
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
        }

        private void save(SeekableByteChannel ch) throws IOException
        {
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
            abc.flush();
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

        public long getSize()
        {
            return size;
        }
        
    }
}
