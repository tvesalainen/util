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
package org.vesalainen.nio.channels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import static java.util.zip.Deflater.*;
import java.util.zip.Inflater;
import org.vesalainen.util.OperatingSystem;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GZIPChannel implements ByteChannel
{
    private static final int BUF_SIZE = 4096;
    public final static short MAGIC = (short)0x8b1f;
    private final static int FTEXT = 1;
    private final static int FHCRC = 2;
    private final static int FEXTRA = 4;
    private final static int FNAME = 8;
    private final static int FCOMMENT = 16;
    private FileChannel channel;
    private boolean readOnly;
    private String filename;
    private Set<OpenOption> options;
    private Inflater inflater;
    private Deflater deflater;
    private ByteBuffer compBuf = ByteBuffer.allocate(BUF_SIZE);
    private byte[] uncompBuf = new byte[BUF_SIZE];
    private final Path path;
    private String comment;
    private CRC32 crc32 = new CRC32();
    private FileTime lastModified;

    public GZIPChannel(Path path, OpenOption... options) throws IOException
    {
        this(path, FileChannel.open(path, options), Arrays.stream(options).collect(Collectors.toSet()));
    }

    public GZIPChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        this(path, FileChannel.open(path, options, attrs), options);
    }

    public GZIPChannel(Path path, FileChannel channel, Set<? extends OpenOption> options)
    {
        this.path = path;
        this.channel = channel;
        this.options = new HashSet<>(options);
        if (options.contains(READ) && options.contains(WRITE))
        {
            throw new UnsupportedOperationException("both read and write not supported");
        }
        if (!options.contains(WRITE))
        {
            this.options.add(READ);
        }
        compBuf.order(LITTLE_ENDIAN);
        compBuf.flip();
    }
    public void compress(Stream<Path> paths) throws IOException
    {
        boolean first = true;
        Iterator<Path> it = paths.iterator();
        while (it.hasNext())
        {
            Path p = it.next();
            if (Files.isRegularFile(path))
            {
                setFilename(p.getFileName().toString());
                setLastModified(Files.getLastModifiedTime(p));
                if (!first)
                {
                    nextOutput();
                }
                first = false;
                OutputStream os = Channels.newOutputStream(this);
                Files.copy(p, os);
            }
            else
            {
                throw new IOException(p+" is not a regular file");
            }
        }
    }
    public void extractAll(Path targetDir, CopyOption... options) throws IOException
    {
        ensureReading();
        do
        {
            Path p = targetDir.resolve(filename);
            InputStream is = Channels.newInputStream(this);
            Files.copy(is, p, options);
            Files.setLastModifiedTime(p, lastModified);
        } while (nextInput());
    }
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        ensureReading();
        try
        {
            int length = Math.min(uncompBuf.length, dst.remaining());
            int rc;
            while ((rc = inflater.inflate(uncompBuf, 0, length)) == 0)
            {
                if (inflater.needsDictionary())
                {
                    throw new IOException("inflater needs dictionary");
                }
                if (inflater.finished())
                {
                    readTrailer();
                    return -1;
                }
                if (inflater.needsInput())
                {
                    fillInflater();
                }
            }
            crc32.update(uncompBuf, 0, rc);
            dst.put(uncompBuf, 0, rc);
            return rc;
        }
        catch (DataFormatException ex)
        {
            throw new IOException(ex);
        }
    }
    private void fillInflater() throws IOException
    {
        if (!compBuf.hasRemaining())
        {
            compBuf.clear();
            channel.read(compBuf);
            compBuf.flip();
        }
        inflater.setInput(compBuf.array(), compBuf.position(), compBuf.remaining());
        compBuf.position(compBuf.limit());
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        ensureWriting();
        int count = src.remaining();
        while (src.hasRemaining())
        {
            if (deflater.needsInput())
            {
                int len = Math.min(src.remaining(), uncompBuf.length);
                src.get(uncompBuf, 0, len);
                deflater.setInput(uncompBuf, 0, len);
                crc32.update(uncompBuf, 0, len);
            }
            compBuf.clear();
            int rc = deflater.deflate(compBuf.array());
            if (rc > 0)
            {
                compBuf.position(rc);
                channel.write(compBuf);
            }
        }
        return count;
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        if (channel.isOpen())
        {
            if (deflater != null)
            {
                writeTrailer();
            }
            channel.close();
        }
    }

    private void ensureReading() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (!options.contains(READ))
        {
            throw new NonReadableChannelException();
        }
        if (inflater == null)
        {
            readHeader();
            inflater = new Inflater(true);
        }
    }

    private void ensureWriting() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (!options.contains(WRITE))
        {
            throw new NonWritableChannelException();
        }
        if (deflater == null)
        {
            writeHeader();
            deflater = new Deflater(DEFAULT_COMPRESSION, true);
        }
    }

    private void readHeader() throws IOException
    {
        filename = null;
        comment = null;
        compBuf.compact();
        channel.read(compBuf);
        compBuf.flip();
        short magic = compBuf.getShort();
        if (magic != MAGIC)
        {
            throw new IllegalArgumentException(path+" not gzip file");
        }
        byte cm = compBuf.get();
        if (cm != 8)
        {
            throw new UnsupportedOperationException(cm+" compression method not supported");
        }
        byte flag = compBuf.get();
        int mtime = compBuf.getInt();
        lastModified = FileTime.from(mtime, TimeUnit.SECONDS);
        byte xfl = compBuf.get();
        byte os = compBuf.get();
        if ((flag & FEXTRA) != 0)
        {
            short xlen = compBuf.getShort();
            compBuf.position(compBuf.position() + xlen);
        }
        if ((flag & FNAME) != 0)
        {
            filename = readString(compBuf);
        }
        if ((flag & FCOMMENT) != 0)
        {
            comment = readString(compBuf);
        }
        if ((flag & FHCRC) != 0)
        {
            ByteBuffer duplicate = compBuf.duplicate();
            duplicate.flip();
            CRC32 crc32 = new CRC32();
            crc32.update(duplicate);
            int value = (int) (crc32.getValue() & 0xffff);
            int crc16 = (compBuf.getShort() & 0xffff);
            if (crc16 != value)
            {
                throw new IOException("CRC16 mismatch");
            }
        }
    }

    private void writeHeader() throws IOException
    {
        compBuf.clear();
        compBuf.putShort(MAGIC);
        compBuf.put((byte)8);
        byte flags = FHCRC;
        if (filename != null)
        {
            flags |= FNAME;
        }
        if (comment != null)
        {
            flags |= FCOMMENT;
        }
        if (lastModified == null)
        {
            lastModified = FileTime.from(Instant.now());
        }
        compBuf.put(flags);
        compBuf.putInt((int) lastModified.to(TimeUnit.SECONDS));
        lastModified = null;
        compBuf.put((byte)2);   // compressor used maximum compression, slowest algorithm
        switch (OperatingSystem.getOperatingSystem())
        {
            case Linux:
                compBuf.put((byte)3);   // unix
                break;
            case Windows:
                compBuf.put((byte)11);  // NTFS filesystem (NT)
                break;
            default:
                compBuf.put((byte)255);  // unknown
                break;
        }
        if (filename != null)
        {
            writeString(compBuf, filename);
        }
        if (comment != null)
        {
            writeString(compBuf, comment);
        }
        filename = null;
        comment = null;
        ByteBuffer duplicate = compBuf.duplicate();
        duplicate.flip();
        CRC32 crc = new CRC32();
        crc.update(duplicate);
        compBuf.putShort((short) (crc.getValue() & 0xffff));
        compBuf.flip();
        channel.write(compBuf);
    }
    
    private String readString(ByteBuffer bb)
    {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        int cc = bb.get() & 0xff;
        while (cc != 0)
        {
            bais.write(cc);
            cc = bb.get() & 0xff;
        }
        return new String(bais.toByteArray(), ISO_8859_1);
    }

    private void readTrailer() throws IOException
    {
        compBuf.position(compBuf.position() - inflater.getRemaining());
        compBuf.compact();
        channel.read(compBuf);
        compBuf.flip();
        int crc = compBuf.getInt();
        long value = crc32.getValue();
        if (crc != (value & 0xffffffff))
        {
            throw new IOException("CRC32 mismatch");
        }
        int isize = compBuf.getInt();
        if (isize != (inflater.getBytesWritten() & 0xffffffff))
        {
            throw new IOException("Size mismatch");
        }
    }
    /**
     * After read returns -1 call nextInput to advance to nextInput file.
     * @return true if nextInput file available.
     * @throws IOException 
     */
    public boolean nextInput() throws IOException
    {
        if (!options.contains(READ) && !inflater.finished())
        {
            throw new IllegalStateException("should be called after read returns -1");
        }
        if (!compBuf.hasRemaining())
        {
            compBuf.clear();
            int rc = channel.read(compBuf);
            if (rc == -1)
            {
                return false;
            }
            compBuf.flip();
        }
        readHeader();
        inflater.reset();
        crc32.reset();
        return true;
    }
    public void nextOutput() throws IOException
    {
        writeTrailer();
        writeHeader();
    }
    /**
     * Returns original filename without path or null name doesn't exist.
     * @return 
     */
    public String getFilename()
    {
        return filename;
    }
    /**
     * Returns comment or null.
     * @return 
     */
    public String getComment()
    {
        return comment;
    }
    /**
     * Sets original filename for compressed file.
     * Set before first write.
     * @param filename 
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
    /**
     * Sets comment for compressed file.
     * Set before first write.
     * @param comment 
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    /**
     * Sets last modified time for compressed file.
     * Set before first write.
     * @param lastModified 
     */
    public void setLastModified(FileTime lastModified)
    {
        this.lastModified = lastModified;
    }

    private void writeString(ByteBuffer bb, String text)
    {
        bb.put(text.getBytes(ISO_8859_1));
        bb.put((byte)0);
    }

    private void writeTrailer() throws IOException
    {
        deflater.finish();
        compBuf.clear();
        int rc = deflater.deflate(compBuf.array());
        compBuf.position(rc);
        compBuf.putInt((int) (crc32.getValue() & 0xffffffffL));
        compBuf.putInt((int) (deflater.getBytesRead() & 0xffffffffL));
        compBuf.flip();
        channel.write(compBuf);
        crc32.reset();
        deflater.reset();
    }

}
